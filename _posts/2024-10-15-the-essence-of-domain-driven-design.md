---
title: "The Essence of Domain-Driven Design"
subtitle: "The philosophy of building software systems: what stands behind the rules of DDD."
description: "The text of my DEV.BG talk about the philosophy behind Domain-Driven Design: shared understanding, definitions, abstraction, complexity, value objects, entities, aggregates, and bounded contexts."
date: 2024-10-15 19:30:00 +0300
categories: [Architecture]
tags: [ddd, domain-modeling, value-objects, entities, aggregates, bounded-context, talks]
example: the-essence-of-domain-driven-design
---

*This is the text of my talk at
[DEV.BG Software Architecture](https://dev.bg/event/software-architecture-filosofiya-na-izgrazhdaneto-na-softuerni-sistemi-ili-the-essence-of-domain-driven-design/)
on 15 October 2024. I gave the talk in Bulgarian. Here it is in English.*

## A Short Introduction: Images and Models

Hello, everyone. This is the first time I speak at such a big forum, so please forgive me
if I am a little nervous.

I decided to start from far away. At the beginning, everything will seem too abstract to
you, but in the second half of the talk the pieces will start to fall into place. Before
we look at DDD, I will ask the following philosophical question: **What makes software
good?**

Let me open a bracket here. I assume that most of you already know DDD, so this
presentation will stay at a higher level.

My goal is to push you to think in the core ideas of DDD, and to highlight the principles
behind this methodology. In a way, I want to create a mental image of this philosophy of
building software models.

If you think about it, education is exactly this: creating images, mental models, and
ideas. The essence is in the word "image"!

> **Lost in translation**
>
> In Bulgarian, the word for education, *образование* (obrazovanie), comes from *образ*
> (obraz), "image".
{: .callout .callout--note}

Unfortunately, in many companies the DDD methodology is not established. Teams work out of
habit, or, if DDD is introduced, it stays too shallow. Nobody explains its essence. So
what I am going to tell you will seem not only abstract, but also unusual.

In general, it is hard to establish good practices, because they need to be promoted
among the teams. This does not happen. Little by little, under the pressure of deadlines,
we do what we know best. Our ego wins, and "our way" is the best way. Is it really? How
many of your ideas have stood the test of time? Because this is the main criterion.

This ends my introduction. To close it, I want to remind you that **knowledge is not
given, it is taken**. No matter how many presentations you attend, you have to show
interest yourselves, start reading, and go deeper.

Today you will not see a concrete implementation. I will present the ideas in principle
and give you the books you need. They contain enough examples.

## Building Shared Understanding

Let's start philosophizing.

Have you ever asked yourselves what our work is? The work of a programmer?

As programmers, we must take a given real-life situation or process and move it into the
virtual world of the computer. In other words, into executable code. This is not an easy
task at all, because we must not only understand the goal, but also describe the process
or the situation in the language of the computer.

The description requires us to know the meaning of the words, and the meaning is given by
a precise definition.

The team that works on the task must build a shared understanding, a "co-knowledge". This
means that we all think in the same way about the task in front of us. Each of us has a
mental model of the domain. If one person understands one thing and another person
something else, we get surprising results.

> **Lost in translation**
>
> The slide title used the Bulgarian word *съ-знание* (sa-znanie). Written as one word,
> it means "consciousness". With the hyphen, it reads literally as "co-knowledge":
> knowing together, just like the Latin *con-scientia*.
{: .callout .callout--note}

Let's look at an example:

```text
All volcanoes are mountains.
All geysers are volcanoes.
--------------------------------------
Therefore, all geysers are mountains.
```

Here the word "volcano" is used in two different senses:

- the first is a raised landform;
- the second is an erupting spring.

### The First Law of Logic

> **The first law of logic**
>
> In a given reasoning, argument, or discussion, every concept must be used in one and the
> same sense.
{: .callout .callout--commandment}

Definitions of the concepts alone are not enough. To draw correct conclusions, we must
follow the laws of logic. If we break them, our work will lead us to wrong results.

There are four laws of logic, but I will not go into them in detail. I highlight only the
first one, because it concerns our topic.

The first conclusion is that the program must be logically correct, but it all starts
with the definition of the concepts.

A small digression. There are also many concepts that are hard to define correctly, and
everyone puts a different meaning into them. For example, the word "freedom".

Unfortunately, politicians and advertisers work with such "watered-down" concepts. Such
concepts must be placed in a specific context.

## How Do We Describe Concepts to the Computer?

> **Key point**
>
> A computer manipulates symbols that reflect the environment.
{: .callout .callout--key}

So, let's assume that we have already agreed on the definitions by discussing them with
the stakeholders.

The computer is blind and deaf, and it has no mind. The only thing we can do is list the
properties of the object.

Properties are symbolic variables that have some value. Together they build a picture for
the "blind" computer. In a way, the computer "touches" them and recognizes what this
concept means.

The analogy is this. As programmers, we look at a class. We read its fields and the
actions performed on them, and this way we understand what the class is for.
Unfortunately, we humans always forget that we are intelligent beings: adding comments
speeds up the work many times.

Anyway... Let's sum up what we have so far.

We have built a shared understanding in the team through definitions, and we describe
reality through the properties of objects, using symbols.

Is this enough, in your opinion? Of course NOT. We need to remove what is unnecessary. We
need abstraction.

## Abstraction

> **Key point**
>
> Abstraction is achieved by taking away and adapting.
{: .callout .callout--key}

It is very hard to build a complete picture, because the description must be as short as
possible, but, on the other hand, complete enough. We cannot have both at the same time.
Therefore, we must concentrate and describe things for the specific case.

Abstraction is achieved by taking away, or in other words, by cutting off. Just as a
sculptor takes away material to reach the right shape.

The rule that we programmers follow is this: first we look at the actions that must be
performed (the future methods in the code). Only then do we look for the properties of the
object (the fields) that can help us implement them.

Short, abstract, and with a lot of talent: this form of writing is called poetry. Let's
look at a popular verse.

### Programmers as Creators

<figure class="post-figure">
  <a href="{{ '/assets/images/the-essence-of-domain-driven-design/poem-as-code.png' | relative_url }}">
    <img src="{{ '/assets/images/the-essence-of-domain-driven-design/poem-as-code.png' | relative_url }}"
         width="789" height="311" loading="lazy"
         alt="A Bulgarian stanza of four lines with hand-written code notes: night =
              true and moonlight = rise above the first line, a red bracket labeled next
              sprint next to lines two and three, and mountain.startTheSong under the
              last line.">
  </a>
  <figcaption>A stanza by Hristo Botev, annotated as code.</figcaption>
</figure>

> Evening falls, the moon rises,<br>
> stars strew the vault of heaven;<br>
> the forest murmurs, the wind blows,<br>
> the Balkan sings a hajduk song!
>
> — Hristo Botev, "Hadji Dimitar" (1873)
{: .epigraph}

> **About the poem**
>
> This is the seventh stanza of "Hadji Dimitar" by Hristo Botev, one of the best-known
> poems in Bulgarian. The translation above is mine and literal. "The Balkan" is the
> Balkan Mountains. A hajduk was a rebel who fought against Ottoman rule. The notes on the
> picture turn the first line into `night = true` and `moonlight = rise`, leave lines two
> and three for the "next sprint", and turn the last line into
> `mountain.startTheSong("Капитан Петко войвода")`. Captain Petko Voivoda was a famous
> hajduk leader.
{: .callout .callout--note}

How would you turn this into code? It is not easy at all. In this case, it is impossible,
because the poetry gets lost.

You must go deep into every word or expression and find its essence. Then you must
explain every assignment: what does it mean?

This is why comments in the code are so important. How many of you write comments on the
fields of a class?

The Balkan cannot sing, but we still gave it this property. We are not bound to the
physical world. We create our own virtual world.

### Programmers as Dreamers

<figure class="post-figure">
  <a href="{{ '/assets/images/the-essence-of-domain-driven-design/metromap-as-abstraction.jpeg' | relative_url }}">
    <img src="{{ '/assets/images/the-essence-of-domain-driven-design/metromap-as-abstraction.jpeg' | relative_url }}"
         width="834" height="786" loading="lazy"
         alt="The Sofia Metro map. Four colored lines, red, yellow, blue, and green, are
              drawn as straight segments with stations, transfer points, and a legend.">
  </a>
  <figcaption>The Sofia Metro map (Metropolitan EAD): colors and straight lines that do
    not exist in reality.</figcaption>
</figure>

We may need to add properties to a concept that are not typical for it, but that help us
express ourselves better. We need them, so to speak, to get the job done.

The metro lines in the example are not colored in reality, but the colors make a clear
separation. And of course, the lines are not that straight either.

To make a good abstraction, we must understand well the goal we are aiming for. In this
case, we want to orient the passengers, not to give a technical description of the metro
lines themselves.

## Kinds of Complexity

The slide named two kinds of complexity:

- **Essential**
- **Accidental** (in Bulgarian, I translated it freely as "organizational")

We work in production, on the assembly line that makes software. Every production needs a
good organization of the work processes. What do I mean?

When a specific task comes to you, it has a complexity of its own: the **essential**
complexity. In many cases, you will be prepared to solve it. These are the typical
techniques that we learn at university or in our first years of practice as programmers.

If this were the only complexity, there would not be so many failed projects.

Unfortunately, building any system also brings another complexity, the so-called
**accidental** complexity. Let's look at it in more detail, because it affects both the
organization of the processes and the architecture.

Imagine that you have to save something in the database. At first glance, it is quite
trivial, but is it really? Besides the save itself, you must:

- Check whether you have the context you need to decide what to save.
- Deal with a database that is not organized well enough, so you need to do "hacks". Are
  you prepared enough? Whom do you ask?
- The record is saved. Is it easy to write tests?
- You write the test, but it sometimes passes and sometimes fails in the GitHub pipeline.
  Why? How do you diagnose it quickly?
- There are many challenges at the level of the organization, too. You use an outdated
  technology. Will you be able to hire new colleagues quickly?
- You have a better idea. How does it reach the people who make decisions?
- After you finish your task, in how many other places must you mark it as done? And so
  on.

It is important to understand that exactly this complexity blocks the development of the
project and turns it into a [Big Ball of Mud](http://www.laputan.org/mud/), not to mention
what it does to people's motivation.

The goal of good companies is to reduce this complexity at the level of the organization.
A successful company is one that keeps the added complexity under control.

## Summary: What Makes Software Good?

So far, we have described the following philosophical directions that we must follow when
we write software:

- building a shared understanding;
- drawing logically correct conclusions;
- describing properties with symbols;
- building abstraction;
- and the last one, keeping complexity under control.

In the books, you will read that a correct description is the first thing that the DDD
methodology deals with. Yes, that is true, but it is not the only thing.

DDD also helps us build the right abstraction, but this abstraction will be much better if
we go deep into the properties of the objects. The DDD methodology does not teach us this;
it only stresses its importance. It does not tell us how to control complexity at all
levels, only at the level of the code.

In other words, this methodology does not cover everything, but it is a very important
part of the process of building software.

> **From an architectural point of view**
>
> Using DDD guarantees that we create a single picture of the problem, with clear
> boundaries in the code. To reach perfection, however, we also need the skill to think
> logically, to create the right abstractions, and to keep complexity under control.
{: .callout .callout--key}

## Describing Concepts Correctly

> **Key point**
>
> Create the ubiquitous language.
{: .callout .callout--key}

I suggest that we start looking at the methodology itself, and try to stress the essence
of its rules.

So... it will not surprise us that DDD suggests starting with the definition and
description of the concepts. These are the nouns in the problem statement.

Together, they are called domain objects, and, as you know, DDD splits them into two
types: Value Objects and Entity Objects.

### Value Objects

The slide compared the theory with a practical example, `Money`:

| Value object (theory)                                                          | `Money` (practice)                              |
|--------------------------------------------------------------------------------|-------------------------------------------------|
| Has attributes (entities or value objects), and they define its equality        | Has `Value` and `Currency`                      |
| Cannot have state-changing methods                                             | Its `Add` method returns a new `Money` instance |

These objects are relatively easy to find, because they can be combined or they take part
in arithmetic operations. When we meet the business and they tell us about some kind of
arithmetic, we are almost certainly talking about Value Objects. Most often, they express
a measure, a quantity, a count, and so on.

DDD describes very strictly the rules that we must follow when we build this kind of
object. Value Objects:

- are immutable;
- have no identifier (ID);
- can validate themselves;
- define `equals` and `hashCode` based on their attributes;
- have `toString` for easy checks in the log;
- can exist in any number in the program;
- define methods that form their algebra;
- have no side effects;
- do not keep state.

The Factory Method pattern is very often used to create them. If they are very large, the
Flyweight pattern is used.

A small architectural digression: it is good to make our application stateless. If it is
stateless, it keeps state only in the database or in the client.

### Entity Objects

The slide compared the theory with a practical example, `Product`:

<figure class="post-figure">
  <a href="{{ '/assets/images/the-essence-of-domain-driven-design/entity-object.png' | relative_url }}">
    <img src="{{ '/assets/images/the-essence-of-domain-driven-design/entity-object.png' | relative_url }}"
         width="1182" height="670" loading="lazy"
         alt="Two diagrams split by a dashed line. Theory: an Entity has an identity that
              defines its equality, has attributes (entities or value objects), and can
              have state-changing methods. Practice: a Product has an ID, Options, a
              Retail price, and a Selling price, and its Change_Price_To method changes
              the Selling price.">
  </a>
  <figcaption>An Entity in theory and in practice: the <code>Product</code>.</figcaption>
</figure>

Entity Objects have a long life cycle! Let's look at this characteristic in more detail.

What do we mean by "life cycle"? Usually, these objects change "drop by drop" until they
reach the desired state, and after a change they are most often saved in the database.
These states must be known in advance, and we must try to keep the system from falling
into uncertainty.

By "uncertainty" I mean that an object must not go into a state that is unknown to us. To
avoid this, for every Value Object we must define its invariants.

What are invariants?

{: .definition}
invariant *noun*
: The truths about an object.

You must ask yourself: what makes the object true? An example: we have the object Hotel.
What makes a hotel a hotel? The rooms. If there are no rooms, it is not a hotel.

Or the box of medical instruments. It holds a certain number of instruments. If this
number is exceeded, the box cannot be closed and the process cannot continue. It turns
into a "non-box".

After every change of these objects, we must make sure that the object is in a valid
state. This is why we have logic in the code that checks it.

When we have the invariants, the logic in the code becomes much simpler. We are sure that
we work in a controlled environment, and we do not need extra checks.

Here are the rules for writing Entity Objects:

1. They are named (natural IDs are preferred).
2. Two objects are equal if they have equal IDs.
3. They have a long life cycle.
4. They are saved in the database.
5. They have rules that forbid certain states (invariants).
6. We decide carefully what can change and what cannot.

A digression. It is very useful that Entity Objects are named, because in some cases it is
convenient to keep a reference to the Entity Object instead of the object itself. We keep
only its ID.

The 6th rule, so to speak, defines the first level of abstraction. An Entity Object works
with some set of properties, but it defines exactly what operations can be performed on
them!

## How Do We Validate Incoming Data?

We have the domain objects, but the data that changes their state comes from outside.

To make correct transitions between states, we need correct input data. You may have
heard the maxim: garbage in, garbage out.

The checks happen in a strictly defined order:

1. **Origin**: Is the data from a legitimate sender?
2. **Size**: Is it reasonably big?
3. **Lexical content**: Does it contain the right tokens and encoding? In other words, do
   we understand every word of the sentence?
4. **Syntax**: Is it in the right format? This is a grammar check: is the sentence
   grammatically correct?
5. **Semantics**: Does the data make sense in the given context? Is it useful to us?

Valid data protects our system from certain kinds of attacks. Our product becomes "secure
by design".

### Parse, Don't Validate

> **Slogan**
>
> Parse, do not validate!
{: .callout .callout--commandment}

You may have met this slogan. Let's see how it fits the rules above.

An example: a JSON document arrives at the input. Turn it into a class. If you cannot, the
data is invalid.

In other words, let the compiler validate the structure of the data. This is why we aim to
type everything and not to work with primitive types. Every parameter, field, or object
must be a domain object.

It is a very bad practice to do validation (checking field by field), because to have good
validation, you practically have to write a parser, and that is hard. **Parsers are a
science.**

The good news is that we rarely have to write the parser ourselves. Most frameworks
already map the incoming request to a class, and this mapping is our structural
validation. We can also validate the request against a schema, for example a JSON Schema
or an OpenAPI specification, before it reaches the domain logic.

### Be Liberal in What You Accept

> **Rule**
>
> Be liberal with what you accept, and conservative in what you emit.
>
> What is valuable for me (in this life)?
{: .callout .callout--commandment}

This is another common rule. In short, read it this way: validate only what concerns you.
Do not put unnecessary things in the result, only as much as the process needs to
continue.

Be careful with what you give. Once a field is in the contract, somebody will start to
depend on it. Taking it back later is a backward-incompatible change. It is always easier
to add a field than to remove one.

## What Should the Database Contain?

From the point of view of DDD, this is a philosophical look at the eternal question.

It is very important to clarify this, because it is key when we describe the different
scenarios. The goal is to shape the right relationship between the domain logic and the
database.

What we keep in the database is actually the foundation on which we build the whole
software product.

DDD is about a strict separation between the domain objects and the database. The
database keeps the relations, and the domain logic interprets them.

Can you give a definition of a relation? We said that before we start working, we must
agree on the definition. The word "relation" is used often, but a precise definition is
hard.

{: .definition}
relation *noun*
: A labeled connection between concepts.

Let's give some examples:

- Ivan *likes* jazz.
- Georgi *has* a guitar.
- Radka *loves* to cook.

"Likes", "has", and "loves" are the labeled connections. That is clear.

The word "concept" also needs an explanation. Dictionaries of synonyms and explanatory
dictionaries do a great job here. A concept is a notion, an understanding, an opinion.

Here is the rule:

> **Key point**
>
> The database keeps our understanding of reality. It describes the real world, or a given
> area of life, work, or relationships.
{: .callout .callout--key}

This is why it is important to agree on which relations exist as a given. These are the
axioms on which we can stand and build the system.

I warned you that my presentation would be unusual :)

An example from our company:

- A box contains one or more medical instruments.
- Instruments are either ours or third-party.
- A repair is approved by the director of the hospital department.

These understandings of ours must be reflected as connections (relations) in the database.

### Mapping Is Not Always 1:1

<figure class="post-figure">
  <a href="{{ '/assets/images/the-essence-of-domain-driven-design/mapping-not-one-to-one.png' | relative_url }}">
    <img src="{{ '/assets/images/the-essence-of-domain-driven-design/mapping-not-one-to-one.png' | relative_url }}"
         width="809" height="708" loading="lazy"
         alt="A hand-drawn sketch. A diamond labeled Domain Objects sits above a box
              labeled DataBase relations. Red shapes labeled Mapping connect each domain
              object to only some of the related records in the database.">
  </a>
  <figcaption>Each domain object maps only a part of the relations in the
    database.</figcaption>
</figure>

The domain logic (the program), in turn, is an interpretation of the objective truth.

What does this mean?

It means that domain objects can take "a part of the reality" that is in the database. For
example, the worker who repairs the medical instrument does not need to know how much
money was paid for the instrument.

In other words, we must go deep into the specific domain and decide which part of the
relations (understandings) must be visible. Then we define their behavior. As you can
guess, these domain objects are Entity Objects.

And one more very important thing. DDD insists that we define the meaning of the concepts
according to the specific domain. This means that the domain logic interprets reality
according to the specific case.

An example: in one domain, the worker can be seen as a craftsman with a certain
qualification. In accounting, the same worker is a first name and a last name with a
certain salary.

## The Abstract View of DDD

<figure class="post-figure">
  <a href="{{ '/assets/images/the-essence-of-domain-driven-design/ddd-abstract-view.png' | relative_url }}">
    <img src="{{ '/assets/images/the-essence-of-domain-driven-design/ddd-abstract-view.png' | relative_url }}"
         width="934" height="620" loading="lazy"
         alt="A hand-drawn diagram of nested boxes. A Data source arrow enters the outer
              box at Data Validation and transformation. It flows into Bounded Context
              creation with Factories, which contains Aggregates with Domain Objects
              inside, and Domain Services.">
  </a>
  <figcaption>The abstract view of DDD: validation first, then factories create the
    bounded context with its aggregates and domain services.</figcaption>
</figure>

Here is what the picture looks like so far. It is too abstract, but it will help us
organize the structure of the project.

What is left to "cover" in this picture?

## Domain Services

> **Domain services**
>
> Domain services encapsulate domain logic and concepts that are not naturally modeled as
> value objects or entities in your model, and are NOT typical CRUD operations.
>
> - They are stateless.
> - They are highly cohesive, meaning they are specialized in doing one thing and one
>   thing only.
{: .callout .callout--key}

Let's start with Domain Services.

Very often, a domain service is a function or a set of functions. In a way, it is simply
an "external service" that the domain logic has to use. Immediately, a familiar picture
appears in your heads: the Util methods, right?

These Util methods are not bad by themselves, but they are a sign that no methodology or
strategy was used to build the product. DDD has such a methodology, and there we follow a
convention: `Class.staticMethod()`.

We try to keep them stateless.

## Aggregates

> **What are aggregates?**
>
> Aggregates are a kind of framework that exposes methods for invoking a command, or
> commands, to make a particular action happen. Actions manipulate domain objects.
>
> An aggregate has a concrete state, and it depends on the current state of the aggregate
> how, and if, certain operations can be performed.
{: .callout .callout--key}

What do we have so far?

Small objects, Entity or Value Objects, and service (external) functions. In practice,
these are our building blocks. You can imagine them as Lego pieces. They have a good
specification, they are distinguishable, and we can compare them. Most important, we can
reuse them to build something bigger.

In software, when we describe an action, it very often changes several of these small
objects. It was the same with Entity Objects: there, the fields of the object changed.

Here is an invented action from our process: "I send a medical instrument for repair."

1. We mark the instrument as unusable.
2. We classify the instrument.
3. We put the instrument in the box for broken instruments.
4. Or we request a new one.

DDD recommends making generalizations. I will perform these actions, but under the hat of
a single method call. I create a new level of abstraction. **The Aggregate Root is a
gateway.**

Why?

Because behind these actions there are many other details that we miss "at first
reading". The instrument is an Entity; so far, so good. But what about "putting it in the
box"? How many instruments at most can go in the box? Are they sorted by type? Do we need
permission?

Of course, we can do it line by line, but this will create problems when we maintain the
code. In a way, we do unnecessary micromanagement at the level of the code.

Aggregates gather several steps into one action. Some sources also call this a "unit of
work".

We ask for the operation to be done without caring about the details. You just call the
aggregate method: `sendForRepair(instrumentId)`.

In essence, aggregates are Entity Objects with a higher, 2nd level of abstraction. They no
longer change fields; they change the states of Entity Objects.

There is a rule in our profession. Follow it!

> **A rule of our profession**
>
> Every problem becomes solvable if you add one more level of abstraction.
{: .callout .callout--commandment}

### Views Only Hide Things

> **Proven by life**
>
> Views only hide things, never remove them.
{: .callout .callout--commandment}

Let's not forget that reality is in the database. Through the aggregates, we may show
only a part of it, but this does not mean that the rest does not exist.

Usually, what we see as users is only the tip of the iceberg.

### Rules for Writing Aggregates

1. The aggregate must be the entry point for every operation with a higher level of
   abstraction.
2. It must not manipulate more than three Entity Objects.
3. It must have a global (universal) ID.
4. It must make sure that all objects are in a valid state.
5. It must enforce the invariants set by the business after every transaction.
6. Aggregates are saved in the database.

These are the rules. I want to draw your attention to two things.

First, the invariants. You probably remember that every Entity object also has
invariants. DDD recommends following "the rules of the game" at all levels. This approach
guarantees the correctness of the program.

Second, saving the aggregate in the database. What is important here?

Imagine the aggregate as one complex, you could even say composite, Entity Object. This
means that it spans more than one table, if we use a relational database.

The benefits are:

- We keep a consistent state in the database (save within a transaction).
- We work with a higher abstraction, which should always be our goal.
- Hibernate is made to work this way. It brings the whole scene of action to the surface.

But we must also ask ourselves the following questions:

- Can we easily do the mapping between the database and the aggregate?
- If we have to change the database "by hand", how easy will the change be?
- What type of database should we choose?

The main problem with this approach is inheritance, if it exists in the code.

### Moving Between States

The slide showed the states of an order as a state diagram:

<figure class="post-figure">
  <a href="{{ '/assets/images/the-essence-of-domain-driven-design/aggregate-states.png' | relative_url }}">
    <img src="{{ '/assets/images/the-essence-of-domain-driven-design/aggregate-states.png' | relative_url }}"
         width="948" height="744" loading="lazy"
         alt="A state diagram of an order. Start leads to Under configuration, then to
              Complete but not paid. From there the order is either Paid or Complete but
              payment rejected, which goes back to Under configuration. Paid leads to
              Shipped. Shipped leads to Delivered or Misplaced; Misplaced leads back to
              Shipped or to Lost. Delivered leads to Rejected, Under return, Returned,
              and Repaid. Lost, Delivered, and Repaid are marked as end states.">
  </a>
  <figcaption>The states of an order. Lost, Delivered, and Repaid are end
    states.</figcaption>
</figure>

I definitely want to mention one good technique for building complex aggregates.

Aggregates, just like entity objects, change their state over time. They have a life
cycle.

It may turn out that the aggregate has many states that it must go through. In such
cases, it is best to implement the description as a finite-state automaton (FSA, plural:
automata).

This way, we describe clearly which events (messages) lead to a change. Also, the code
becomes much more readable.

### Make Illegal States Unrepresentable

> **The goal of all constraints**
>
> Make illegal states unrepresentable!
{: .callout .callout--commandment}

It is important that our system never goes into an undefined state. As we said, this must
be our maxim when we construct the objects and validate the data.

This way, our domain logic will be free of exceptions, and that makes the code much
cleaner.

## Creating the Bounded Context

> **Key point**
>
> Aggregates should be instantiated.
{: .callout .callout--key}

This is the last popular DDD term that I want to talk about.

A bounded context consists of several aggregates. It provides their working environment.
It is the stage on which the action takes place. Roughly speaking, it is a separate
microservice.

How to define it is another philosophical question, outside the scope of this talk. The
most popular methodology for defining it is called
[EventStorming]({% post_url 2026-11-26-event-storming %}).

So, for something to work in our system, it must first be instantiated (created).

We must choose the moment of instantiation very carefully, and it must be flexible enough.
For this purpose, creational patterns are used most often.

Here is what we must observe:

- It happens at the right moment: in advance (cache) or just before use. Lazy loading or
  immediate loading.
- The correctness of the created object is guaranteed: the Builder pattern.
- It is fast enough and saves memory: the Flyweight pattern.
- It is flexible, in other words, it covers many cases: the Template Method pattern.

In general, it is not hard to implement a bounded context. The hard part is defining its
boundaries. How big should it be, and what should it contain?

Let's move to the final part, because this was the last topic I wanted to touch.

## In One Picture

Here are all the building blocks of DDD in one picture:

<figure class="post-figure">
  <a href="{{ '/assets/images/the-essence-of-domain-driven-design/whole-picture.png' | relative_url }}">
    <img src="{{ '/assets/images/the-essence-of-domain-driven-design/whole-picture.png' | relative_url }}"
         width="1220" height="766" loading="lazy"
         alt="The building blocks of DDD. A Client calls Application Services and a
              Factory. Application Services orchestrate only and contain no business
              logic. The Factory creates complex entities, value objects, and aggregates.
              Application Services update the model through the aggregate root and use a
              Repository to persist and hydrate aggregates. The Domain Model holds
              Aggregates A, B, and C, Entities, Value objects, and Domain Services. All of
              this sits inside the Bounded Context.">
  </a>
  <figcaption>The building blocks of DDD inside one bounded context.</figcaption>
</figure>

Let's follow a request through the picture. The client calls an application service. The
application service only orchestrates; it contains no business logic. When something new
must be created, a factory creates it. When something must change, the change goes through
the aggregate root. The repository saves the aggregates and loads them back.

The business policies and logic live in the domain model: aggregates built from entities
and value objects, plus the domain services. And all of this lives inside one bounded
context.

The conclusions:

- Don't be verbose. Don't do micromanagement at the level of the code. If your code is too
  verbose, the abstraction is not correct. In the same way, if your code needs many
  comments, it does not explain itself.
- "Fire and forget" must be our motto. Build an abstraction, and an implementation behind
  it, that is so reliable that you simply fire the call. How the process goes in detail
  should not matter to you.
- DDD gives us recommendations on how to build our system at several levels of
  abstraction: entity, aggregate, and bounded context.

> **Key point**
>
> The level of professionalism in our profession is measured by the ability to build
> abstractions at different levels.
{: .callout .callout--key}

With these levels, adding new features and testing become much easier. Try to put every
addition to the product at the right level of abstraction.

The aggregate is not the last level of abstraction; you can continue. For example, you can
combine two or more microservices into a new aggregate of a higher level.

DDD does not cover the system requirements directly.

And now, the books.

## Books

**Recommend a book:**

- Scott Millett and Nick Tune,
  [*Patterns, Principles, and Practices of Domain-Driven Design*](https://www.wiley.com/en-us/Patterns%2C+Principles%2C+and+Practices+of+Domain+Driven+Design-p-9781118714706)
  (Wrox, 2015). DDD, with C# examples.
- Alan Shalloway and James R. Trott,
  [*Design Patterns Explained: A New Perspective on Object-Oriented Design*](https://www.informit.com/store/design-patterns-explained-a-new-perspective-on-object-9780321247148),
  2nd edition (Addison-Wesley, 2004). The philosophy of patterns, with Java examples.

**Recommend a practical book, please:**

- Dan Bergh Johnsson, Daniel Deogun, and Daniel Sawano,
  [*Secure by Design*](https://www.manning.com/books/secure-by-design) (Manning, 2019).
  DDD as security, with Java examples.
- Tom Hombergs,
  [*Get Your Hands Dirty on Clean Architecture*](https://www.packtpub.com/en-us/product/get-your-hands-dirty-on-clean-architecture-9781805128373),
  2nd edition (Packt, 2023). A DDD implementation, with Java examples.
