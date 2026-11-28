# Modules and Layers

The design note says which contexts exist and which events cross between them. This file
says how to keep those borders in the code, so that a wrong dependency fails a test
instead of waiting for a reviewer.

## One Context, One Module

| Rule | How |
| --- | --- |
| One bounded context is one module | A top-level package (or project, or service) per context |
| Other modules see only the public events | Public types live in `events`; everything else lives in `internal` |
| Each module names what it may use | A list of allowed dependencies per module. Anything else fails the build. |
| Dependencies have no cycles | Events flow one way. If two contexts must react to each other, the design is wrong. |
| The shared kernel stays small | Only values that mean the same in every context: `Money`, `InstrumentId` |
| A technical module holds no domain code | `apiguard` limits the request size and writes the error answers; it knows no context |

In the example, the department may use only the shared kernel. The workshop may also use
`department :: events`, and accounting may also use `workshop :: events`. The flow goes
one way: department, then workshop, then accounting.

## Three Layers in Each Module

```text
infrastructure  ->  application  ->  domain
(web, database,     (use cases,      (aggregates, value objects,
 messaging)          ports)           domain services, events)
```

| Layer | Contains | May depend on |
| --- | --- | --- |
| Domain | the model and its rules | the language and the shared kernel only |
| Application | one use case per command or query; ports | the domain; transactions |
| Infrastructure | adapters for web, database and messaging | the application and the domain |

* The domain is plain code. It knows nothing about the framework, HTTP, JSON or SQL.
* A port is a contract. A port that does something is an interface; a port that carries
  data is a record. An incoming port is a use case, and an outgoing port is a
  repository or an event publisher.
* The web adapter calls use cases only. It never loads or changes an aggregate itself.
* The domain has its own events. A messaging adapter maps them to the public events, so
  the internal model never leaves the module.

## One Command, One Transaction

```text
load the aggregate (with a lock) -> call the root -> save it -> publish its events
```

The root collects the events it raises while it works. The use case saves the aggregate
and then hands over the events, in the same transaction. If the save fails, nothing is
published.

The listeners in other modules:

* run after the sender commits, in their own transaction;
* do not make the sender wait, and cannot make it fail;
* get the event again if they fail. The framework keeps each event until its listener
  succeeds.
* are idempotent: the same event twice changes nothing.

## Guard Tests

Write these tests before the first adapter, so that the first wrong dependency fails.

| Test | It checks |
| --- | --- |
| `modulesKeepTheirBoundaries` | only public events are used, the allowed dependencies, no cycles |
| `domainIsPlainJava` | the domain and the shared kernel depend on the language only |
| `applicationKnowsOnlyTheDomain` | use cases know nothing about HTTP, JSON or SQL |
| `portsAreContracts` | every port is an interface or a record |
| `webCallsUseCasesOnly` | the web adapter does not touch aggregates, outgoing ports or services |
| `dependenciesPointInward` | infrastructure, then application, then domain, never back |
| `publicEventsHidePrivateTypes` | a public event uses only public types and the shared kernel |
| `modulesListenAfterCommit` | every listener runs after commit, in its own transaction |

In the example, the first test uses Spring Modulith (`ApplicationModules.verify()`), and
the others are ArchUnit rules in `HexagonalArchitectureTest`.

Without such tools, write a test that reads the imports of each source file and fails on
a forbidden one. It is cruder, but it still turns a design rule into a failing build.
