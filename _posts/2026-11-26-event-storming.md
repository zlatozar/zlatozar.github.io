---
title: "EventStorming: From Sticky Notes to Bounded Contexts"
subtitle: "A practical guide to running an EventStorming session, from the first orange note to microservice boundaries."
description: "How to run an EventStorming session step by step: events, definitions, concerns, commands, external systems, policies, read models, aggregates, and drawing bounded contexts."
date: 2026-11-26 13:04:52 +0200
categories: [Architecture]
tags: [event-storming, ddd, domain-modeling, bounded-context, aggregates, microservices]
---

> Garbage in, garbage out.
>
> — Early computing saying (1950s)
{: .epigraph}

## The Problem We Need to Solve

One of the stakeholders has an idea in their head. They want us to turn it into a software
application. **How is this supposed to happen?**

The best option is to write the idea down as a specification. Then we turn the
specification into a program, in the programming language of our choice. So far, so good.
But **what is a specification?**

{: .definition}
specification *noun*
: A step-by-step description of the process, with definitions given by the domain experts.

It is quite easy to understand what the experts want. What we need is a convenient way to
turn it into an executable program. This program should be:

- broken into small modules (for fast builds and scalability)
- easy to understand for newcomers (or for you, six months later)
- maintainable (you can see the use cases at a glance)
- easy to extend with new features
- testable in isolation
- self-documenting

If these requirements are not met, sooner or later the program becomes a
[big ball of mud](http://www.laputan.org/mud/).

> **Key point**
>
> Misunderstood requirements add accidental complexity to a business domain that is
> already complex.
{: .callout .callout--key}

All engineering problems are solved on the drawing board first, right? Let's begin.

## What Is EventStorming?

**EventStorming** is a workshop format created by Alberto Brandolini in 2013. EventStorming
is a form of organizational anthropology. It is all about discussing the flow of events in
your organization and modeling that flow in a way that is easy to understand. The
knowledge from an EventStorming session will later feed into other modeling techniques,
which give structure to the business flows that emerge. But the real value is in the
conversations. You can build a software system from the models. Or you can simply use
what you learned in the conversations to understand and improve the business processes
themselves. After all,
[software engineering is a learning process, and working code is a side effect](https://www.lambdabytes.io/posts/selearning/).

You can look at EventStorming in several ways:

- as a step in translating the specification into a program
- as a way for programmers to show their understanding in a language that domain experts
  can follow
- as a way to show stakeholders how the program will work

Brandolini describes three formats. They differ in scope and in who is in the room:

- **Big Picture:** the whole business, or a large part of it. Many people, mostly events
  and hot spots. The goal is a shared understanding and finding the biggest problems.
- **Process Modeling:** one business process from start to end. It adds actors, commands,
  policies, read models, and external systems.
- **Software Design:** the same process, now with aggregates and candidate bounded
  contexts. It is the direct input for the code.

This guide follows one small session. It starts with a short, Big Picture-style timeline
of events, continues as Process Modeling (from "Commands" to "Read Models"), and ends with
Software Design (aggregates and boundaries). For a large domain with many teams, run a
Big Picture workshop first. Then model each interesting process in a separate session.

The important thing is that EventStorming happens with both *programmers* and *domain
experts* in the room. So **programmer jargon**, such as "it will be stored in the
database" or "invalidate the cache", must not be used. The aim is to speak one common
language. In Domain-Driven Design (DDD) it is called the **ubiquitous language**. This
means that we start from the words and definitions of the domain experts and refine them
together with the experts. Programmers do not invent their own names for business
concepts.

The second important thing is to agree on how the meeting will go: *break the work into
small steps and discuss possible scenarios from a business point of view.*

To keep it understandable for people who are not programmers, we use only colorful sticky
notes and a whiteboard. Most often it is a digital whiteboard, because it always has
enough space and never runs out of notes. It is also easy to move notes around quickly,
which is a big advantage.

> **Facilitator tip**
>
> Everything you discuss must go on the board as a sticky note: user, policy, command, and
> so on. If you plan to discuss or check something later, add a concern note (a hot spot).
{: .callout .callout--facilitator}

## Organizing a Typical EventStorming Session

**What do we need to get started?**

- First, a non-trivial problem in a specific domain that we want to automate, and a domain
  expert who can explain it to us.
- Second, and much less important: a digital whiteboard with colorful sticky notes.

The experts or stakeholders take the first step: they explain the task in a few
introductory words. One person "owns" the whiteboard and facilitates the dialogue between
the participants. Often this is one of the programmers, but it does not have to be. This
person explains the rules of the "game". They also save the key steps as a document, so
that we can return to them later to reorganize things or add new features.

## In the Beginning Was the Empty Whiteboard

It always starts with a whiteboard. The facilitator prepares it like this:

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/whiteboard.jpg' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/whiteboard.jpg' | relative_url }}"
         width="1641" height="951" loading="lazy"
         alt="An empty board with a color legend on the left, a Definitions area at the
              top, and a timeline arrow at the bottom that points to the right.">
  </a>
  <figcaption>The empty board: a legend on the left, definitions at the top, and a
    timeline that runs from left to right.</figcaption>
</figure>

This way, everyone understands that the process runs from **left** to **right**, and knows
where to look for the definitions of terms. Make it clear that when things happen *at the
same time*, their notes are stacked vertically.

> **Note**
>
> The exact colors do not matter. What matters is that the legend is always visible
> and that everyone uses it the same way.
{: .callout .callout--note}

## The Facilitator

The facilitator is responsible for the organization: who to invite and how to make people
*more active*. Invite people with questions (programmers, testers, designers) and people
with answers (domain experts). By "more active" I mean asking questions, and adding or
rearranging sticky notes. The facilitator stays neutral: they guide the process, not the
decisions. **As the saying goes, success is a collective effort.**

> **Facilitator tip**
>
> Every participant can add sticky notes, so it is a good idea to share the board.
> [Miro](https://miro.com), [Mural](https://www.mural.co), and
> [FigJam](https://www.figma.com/figjam/) support this.
>
> Open two whiteboards: one for the session and one to save its stages.
{: .callout .callout--facilitator}

## Every Long Journey Begins with the First Step

The hardest part is the beginning, or more exactly, finding the thing that "ignites" the
process.

Do not forget that many things happen by chance. The deeper we go into the topic, the more
accurate our judgments become. So we can, and must, rework the previous steps. **That is
incremental refinement.** We cannot expect our first attempt to succeed. But because we
draw a picture instead of writing a document, we understand each other easily and changes
are cheap. So this should not bother us.

To start, we imagine that *something has already happened* and put a sticky note on the
whiteboard for it. The note says what happened, in the **past tense**, as a noun plus a
verb in the past tense: `Noun + Verb (past tense)`. For example, **"Order placed"** or
**"Request verified"**. Place it close to the legend, but not too close, because later we
may discover that something else must have happened before it. If you have assumptions,
resolve them **later**. For now, they are not a problem.

> **Modeling tip**
>
> Try not to use CRUD terms such as "updated" and "deleted". Ask what the change means for
> the business: "Address changed" or "Order cancelled", not "Order updated".
{: .callout .callout--modeling}

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/order-placed.jpg' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/order-placed.jpg' | relative_url }}"
         width="1588" height="983" loading="lazy"
         alt="The board with one orange event note, Order placed, near the legend.">
  </a>
  <figcaption>The first event, placed close to the legend.</figcaption>
</figure>

## From Event, to Event to the End

A domain event (orange) represents something that changes the state of your business and
that other parts of the system care about.

- If an action only changes what the user sees on the screen (a filter, a sort order, an
  opened tab), it is not a domain event.
- If an action changes something the business cares about (an order is placed, a payment
  is received, a customer is registered), then it **is** a domain event.

**So what is an event?** It is a **fact**. You cannot change it, just as you cannot go
back in time. We use it to mark what is done up to this point in time. Once an event is on
the board, the facilitator asks: **"What should happen next?"** *Take the smallest
possible step in the process.* We leave all technical steps out of the discussion and
place only events that the business cares about. Remember, domain experts are not
programmers.

> **Facilitator tip**
>
> Keep asking questions! **What comes next? Are the existing events in the right order?
> Could we change this flow?**
{: .callout .callout--facilitator}

Sometimes stakeholders do not have the most effective plan. They follow a process they
imagined themselves, or one created by
[their organization](https://www.thoughtworks.com/insights/blog/applying-conways-law-improve-your-software-development),
which is usually not optimized. Once the process is drawn on the board, it is easy to
question. *Be the driver of change.*

> **Key point**
>
> An event is the record of a state change that has already happened. We cannot reject an
> event, so we control what causes it: the *command*. A command can come from a person, a
> policy, or a timer. It is a request, and it can be rejected. The part of the system that
> owns the data (later we call it the aggregate) checks its rules first, and only then
> records the event. This way, every event leaves the system in a valid, known state. We
> must also understand *why* each event happened, so that we can explain the reactions it
> causes.
{: .callout .callout--key}

### From Chaos to a Timeline

Our example grows one note at a time, because it is small. With a larger domain and more
people, Brandolini suggests a faster start:

1. **Chaotic exploration.** Everyone writes events at the same time, for 10–15 minutes,
   without discussion. Duplicates and a wrong order are fine at this point.
2. **Enforce the timeline.** Together, sort the notes from left to right and remove
   duplicates. Put a concern note on every disagreement and move on.
3. **Find pivotal events.** Mark the few events that change the process the most, for
   example *Order placed* or *Order shipped*, with a vertical line. They split the
   timeline into parts, and these parts are often the first hints of bounded contexts.
4. **Walk the story.** One person tells the story from left to right. Then walk it
   backwards and ask for each event: "What must have happened before this?" Missing
   events show up quickly.

> **Facilitator tip**
>
> Timebox each step. A short step with a visible result keeps people active.
{: .callout .callout--facilitator}

## Definitions of Terms

If we decide to use the terms of the experts who give us the task, we must know what those
terms mean, right? That is why we introduce a new kind of note: the light green sticky
note. On it, we briefly describe terms whose meaning we do not know or that are
ambiguous. *Definitions keep the discussion consistent!*

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/definitions.jpg' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/definitions.jpg' | relative_url }}"
         width="1588" height="983" loading="lazy"
         alt="Two green notes in the Definitions area. SKU: the smallest unit to buy.
              Order: list of SKUs.">
  </a>
  <figcaption>Light green notes at the top of the board define the terms.</figcaption>
</figure>

The [law of identity](https://en.wikipedia.org/wiki/Law_of_identity): if we talk about
something, we must be sure that we are all talking about the same thing. That is why we
need definitions.

If two groups of experts use the same word with different meanings, write both
definitions and mark each with its group. Do not force one meaning. This is one of the
best hints for a bounded context boundary, which we will draw later.

## Concerns (Hot Spots)

Concerns, open questions, and disagreements come up during the conversation. Do not stop
to solve them. Write each one on a purple note and put it next to the place where it came
up. We call these notes **hot spots**. Some can be resolved later. For others, it
is good to think more carefully about their **software solution**. If there are too many
of them on the board, it is a sign that:

- the area we will work on is not well known,
- or we have not invited the right people,
- or, what matters most to us as programmers, the implementation will be quite difficult.

It is vital to give these notes priorities (using tags on the notes!) and to work on them
in order of priority. At the end of the session, let everyone vote (for example, with
dots) on the concerns that matter most.

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/concerns.jpg' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/concerns.jpg' | relative_url }}"
         width="1645" height="949" loading="lazy"
         alt="The board legend now has a purple Concerns note under Events and
              Definitions.">
  </a>
  <figcaption>Purple notes record concerns.</figcaption>
</figure>

> **Facilitator tip**
>
> When a discussion takes too long, write it on a concern note and move on. The note keeps
> the question visible, and the session keeps its speed.
{: .callout .callout--facilitator}

## Commands, or Who (Command) Triggers What (Event)

A **command** is a request to the system to do something. It carries an intent: an
action, an interaction, or a decision. A command can be rejected. If it is accepted, it
has an effect: the receiving side changes its state, starts a task, or sends more
commands.

*Fine, we have the events on the board. But are they caused by someone or something?* By
"someone" I mean a user, a scheduler, or the software itself (the program reacts, like a
falling domino). The request that asks the system to change is always a `Command`. Only
the sender differs: a person or a machine. We write commands in the **imperative** mood,
on blue sticky notes.

A command appears on the board in one of two places:

1. **After an actor (front-loading).** A person sees some data, decides to act, and
   issues the command: `[Read Model]` → `[Actor]` → `[Command]` → `[Event]`.
2. **After a policy (back-loading).** The system reacts to a past event and issues the
   next command by itself: `[Event]` → `[Policy]` → `[Command]` → `[Next Event]`.

We look at both loops in more detail in [Policies](#policies-reactive-logic).

### What Is a Reaction?

A **reaction** is something that must happen because something else has happened. One
simple sentence captures it: **"Do this *whenever* that happens."** "This" is the
reaction, and "that" is the event. **The word "whenever" links the event to the reaction,
and together they form a policy (a plan of action).** When people describe a process with
"whenever", they are already describing an *event-driven system*.

A small side note. If you think about it, this is not only about software. There is always
a reason, voluntary or not, for something to happen, and then the fact (event) is there.
Once we have it, we can act. In other words, we apply different policies, as we will see
later.

We must keep reviewing the things we put on the board. Now is the time to ask: **"Is this
the right flow?"** or **"Who raises this command at this time? With what rights?"** The
answers can lead to a rearrangement. Keep in mind that stakeholders do not know the best
algorithms, but they know the process best. Keep asking.

We add a small yellow sticky note to a command when a person issues it. This note is the
**actor**. Write the role of the person, for example *Customer* or *Support*, not the name
of a specific person. On our board, the note *User rights* also reminds us that the actor
needs certain rights to place an order. When a command has no actor note, a policy or a
timer issues it: it is triggered by the system.

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/command.jpg' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/command.jpg' | relative_url }}"
         width="1568" height="996" loading="lazy"
         alt="A blue Place order command with a small yellow User rights note. After it,
              two stacked orange events: Order placed and Audit log created.">
  </a>
  <figcaption>A blue command in front of its event. The small yellow note shows who may
    issue it.</figcaption>
</figure>

*When we communicate with commands, we must think about failures. Why does a command
fail?* A command can be rejected for a **business** reason (for example, not enough
credit). A rejected command changes nothing. But if the business cares about the
rejection, put it on the board as an event, for example *Payment declined*. Then ask the
experts what **compensating actions** the system must take for the steps that already
happened, for example *Cancel order* after *Order placed*. These actions are also
commands. When the system triggers them itself, through a policy, they do not get a
yellow sticky note. When a person must do them (for example, a support person calls the
customer), add the actor note.

## External Systems

These are systems we have **no control** over, and we do not know the details of how they
work. We must show the connection to them.

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/external-system.jpg' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/external-system.jpg' | relative_url }}"
         width="1530" height="1020" loading="lazy"
         alt="The Validate user command calls a red Backend System note, which returns to
              the User validated event. Two purple concerns next to it: It is not
              reliable, and Could slow down the whole system.">
  </a>
  <figcaption>A red note marks an external system and its connections. Purple notes record
    concerns about it.</figcaption>
</figure>

> **Modeling tip**
>
> Never place a command such as **"Ask external system"**. That is a technical detail. In
> the first phase, when you start with the events, focus on business facts. Add the
> external system later, when you ask what causes an event. Instead of "Ask external
> system", place a command that has business value, for example **"Validate user"**.
{: .callout .callout--modeling}

These systems are especially important, because they can change in a direction we do not
want. In Domain-Driven Design, the usual practice is to put a so-called
[anticorruption layer](https://martinfowler.com/articles/refactoring-external-service.html)
between their model and ours. In short, we take the data we need from the external system
and translate it into the terms of **our** model, so their changes stay in one place. The
same adapter is a good place to protect ourselves when the connection fails, with caching
and request retries. But that is a technical concern, not part of the translation.

## Events Triggered by Time

Yes, this is also possible. Some events happen only because time has passed: the end of
the day, a deadline, or a timeout. No user issues a command here, so on our board the
arrow goes directly from one event to the next: `Event` → `Event`.

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/time-triggered-events.jpg' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/time-triggered-events.jpg' | relative_url }}"
         width="1556" height="1003" loading="lazy"
         alt="The event Log rotating ran over one hour, with an arrow to the event
              Rotating log failed.">
  </a>
  <figcaption>A time event leads to the next event. The rule between them is
    implied.</figcaption>
</figure>

Read the board like this. **"Log rotating ran over one hour"** is the time event: a clock
or a timer tells us that one hour has passed. Strictly speaking, a rule is hidden between
the two notes: *"Whenever log rotation runs longer than one hour, stop it and mark it as
failed."* That rule is a policy, and it issues a command. The direct arrow is a useful
shortcut on a quick board. When the rule matters to the business, make it visible: add
the policy and the command between the two events.

> **Modeling tip**
>
> Draw a small clock on time events, so they stand out. Our example comes from operations.
> In a business process, typical time events are *"Payment deadline passed"*, *"Day
> closed"*, or *"Trial period ended"*.
{: .callout .callout--modeling}

**This covers all kinds of time-triggered events: cron jobs, monitors, and schedulers.**

## Policies (Reactive Logic)

A policy automatically triggers a new action (for example, "Send rejection email").

An event together with the reaction to it forms a **policy**. The name fits, because a
policy is a business rule, for example: **"Whenever someone changes their configuration,
alert the customer."**

A reaction, or a whole policy, does not have to be software. For example, "activate
order" can be a manual step: a support person checks the details by hand. So
EventStorming helps you to discover the manual processes in your organization, not only
to design new software.

To show a manual policy, put a small yellow actor note on it. On our board in the next
section, the *Support* note on *Inform support team* means that a support person does
this step.

> **Facilitator tip**
>
> When you hear people say "Whenever this happens, we do that", it is a policy. You can
> start a policy with "Whenever …".
{: .callout .callout--facilitator}

From the domain experts' point of view, this is the most important part of the model. We
are building a kind of crossroads: a place where we must make a decision. Note that the
domain experts decide which roads exist. At this stage we cannot ask questions such as
**"What if there is no connection?"** or **"What if the system crashes?"** Those questions
belong to a later, technical stage of system design. Policies are the **"possible
consequences"** of an event (fact).

The question is **"What do we do when this event shows up?"** or **"What are the next
commands that will complete the next step?"** That is why policy notes are placed after
the fact (event). Policies should show which commands must be issued next, so that the
system can move to its *next state*. Note that a policy can issue more than one command,
and these commands can run in parallel. The number comes from the business rule, not from
technical needs such as speed. Still, keep it low: every extra command is one more path to
test and maintain. A policy can also *choose* one command out of several, based on a
condition. For example: whenever *Payment failed*, if it is the first attempt, *Retry
payment*; otherwise, *Cancel order*. Write the condition on the policy note. (In code,
such a choice is often a Strategy pattern, but that is a detail for later.)

Policies make us look at the system from all angles. At the same time, at this stage we
get an overall view. The questions to ask are **"Great, we have this fact. How does it
change the system?"** and **"Who is waiting for this fact to appear, so they can react?"**
There are always side paths.

More informally, a policy is a `When–Then` clause: `When` an event shows up, `Then`
trigger the matching command(s).

*Most failures in computer systems start with a wrong assumption.* When we model only
cause and effect, we can better decide how to react to anything. We do not make false
assumptions about outcomes: not only successful outcomes, but also errors and other
less-than-ideal events.

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/policy.jpg' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/policy.jpg' | relative_url }}"
         width="1527" height="1023" loading="lazy"
         alt="Pink policy notes after events: New order after Order placed leads to the
              commands Validate user and Create audit log; Valid user after User validated
              leads to Request configuration.">
  </a>
  <figcaption>Pink policy notes sit after an event and lead to the next
    commands.</figcaption>
</figure>

> **Modeling tip**
>
> On our board, *User validated* is followed by two policies, *Valid user* and *Invalid
> user*. This hides two different outcomes in one event. It is clearer to use two events,
> *User validated* and *User rejected*, each with its own policy. The bounded context
> diagram later in this article does exactly this.
{: .callout .callout--modeling}

One policy can trigger more than one command, or none. If it triggers none, we simply
remove the policy note. It is also fine to have policies with the same name after
different events. This shows that different events trigger the same policy.

So a policy listens to domain events and, when its condition holds, issues commands. This
is **reactive behavior**. Systems built mostly from such `Event` → `Policy` → `Command`
chains are *event-driven*, and this is one way to build **reactive systems**.

Now we can look at the two loops from
[Commands](#commands-or-who-command-triggers-what-event) in more detail:

1. **The user-driven loop** (`Command` → `Event`). This is the most common path. A user
   (or another external system) wants to make something happen. They have an intent.
   - **Read model (green):** before deciding, the user needs information. They look at a
     read model (for example, a "Shopping cart" or "Product availability").
   - **Command (blue):** the user acts on that information and issues a command (for
     example, *Place order* or *Check out*). The command represents their intent.
   - **Domain event (orange):** if the aggregate accepts the command, it publishes a
     domain event (for example, *Order placed* or *Payment received*). This is now a fact
     that cannot change.
2. **The system reactive loop** (`Event` → `Policy` → `Command`). This is the automated
   "falling domino" part of your system. Something has already happened, and now the
   system must react to that fact.
   - **Domain event (orange):** the loop starts with a domain event (for example, *Order
     placed*). This event is a fact that has just happened.
   - **Policy (pink on our boards):** a policy (the "whenever" rule) listens for this
     specific event. The policy's job is to **decide**: "Whenever this event happens, what
     automated action do we take next?" (For example, whenever *Order placed* happens, we
     need to prepare the shipment.)
   - **Command (blue):** the policy then automatically triggers a new command (for
     example, *Prepare shipment*). This command starts its own loop: it goes to another
     aggregate and produces another event (for example, *Shipment prepared*).

## Read Models, or Data Requirements

A read model can be a specific part of the UI, or a record that the user needs in order to
carry out a command (action). **It represents what the user or system needs to see before
taking action.** A read model includes all the data that must be available before an
action can be taken, whether it comes from the database or from an external system. Note
that a read model is not part of the aggregate. It is only a way to present data to the
user or system.

Do not try to draw every screen or every specific type. Use read models to highlight
important data requirements. Use them when manual work is needed, for example by a support
team.

Place a read model where a decision is made:

- **In front of a command that a user issues,** when the user needs data to decide. On our
  board, *Configuration* stands before *Place order*.
- **Between an event and a policy,** when the policy needs data to decide what to do next.
  On our board, *Log rotating error* stands between *Rotating log failed* and *Inform
  support team*.

Events update read models, and read models feed decisions. A read model never stands
between a command and its event.

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/read-model.jpg' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/read-model.jpg' | relative_url }}"
         width="1484" height="1052" loading="lazy"
         alt="Green read model notes on the board: Configuration in front of the Place
              order command, and Log rotating error after the Rotating log failed event,
              followed by the Inform support team policy with a yellow Support role.">
  </a>
  <figcaption>Green read model notes show the data needed before an
    action.</figcaption>
</figure>

> **Modeling tip**
>
> You can use a read model to outline a very basic UI that triggers a command. What must
> the user know before pressing the button?
{: .callout .callout--modeling}

### Read Models in Code (Views)

On the board, a read model is the information a person (or a policy) needs to make a
decision. In code, it is often a **projection**, also called a **view**: a read-only data
shape built from domain events for one screen or one query. This is the read side of
CQRS (Command Query Responsibility Segregation). A projection is updated when events
arrive, usually asynchronously, so it can be a little behind the aggregates. Nobody
writes to it directly. To change data, send a command to an aggregate.

> **Modeling tip**
>
> When you look for aggregates, ask: **"Is the data we are changing part of an invariant?
> Can the user change it directly?"** If not, it is a read model (a view), not an
> aggregate. You are modeling data, not a process.
{: .callout .callout--modeling}

## Aggregates: Focus on Behavior, Not Data

*Think of an aggregate as a group of commands that must check the same business rules on
the same data. It is the only way to change that data. Aggregates are the **invariant
keepers** of the system. They keep their own state consistent and control the moves
between states.*

> **Note**
>
> Do not confuse the large yellow aggregate note with the small yellow actor note. The
> actor is the person (role) who issues a command. The aggregate is the part of the system
> that accepts or rejects that command. Newer EventStorming material calls the large
> yellow note a *constraint*, because business people rarely say "aggregate".
{: .callout .callout--note}

From the programmers' point of view, this is perhaps the most important step. Aggregates
are the building blocks for the next step, where we draw the bounded contexts and, later,
the microservices. Do not mix up these three boundaries:

- An **aggregate** is a *consistency* boundary. Its rules (invariants) are true after
  every command, and one transaction changes only one aggregate.
- A **bounded context** is a *model and language* boundary. Inside it, every term has one
  meaning. It usually holds several aggregates.
- A **microservice** is a *deployment* boundary. It often matches one bounded context. But
  one context can be split into several services, and one service should not span two
  contexts.

The idea is to group the commands that must check the same business rules on the same
data in the domain. On top of this group we build access logic and define rules for how
an outside user can invoke the commands. This way we take control and check that all
invariants hold. That role belongs to the **aggregate**: the entry point for every
outside call that wants to change its data (trigger an event through a command). **The
aggregate rules the moves between states.** In the next phase of design, when we turn the
model into code, we must provide methods that change this state.

An aggregate groups the commands it accepts and the events it records. Each command is
handled by exactly one aggregate. The aggregate checks its rules against its current
data and, if the command is accepted, records one or more events. Reactions (policies)
are *not* inside the aggregate. They sit between aggregates: they listen to events and
send commands, often to another aggregate or another context.

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/aggregate-commands-events.svg' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/aggregate-commands-events.svg' | relative_url }}"
         width="610" height="320" loading="lazy"
         alt="Three blue commands on the left, one tall yellow Order aggregate in the
              middle, and orange events on the right. Each command produces one or more
              events.">
  </a>
  <figcaption>The aggregate sits between its commands and the events they produce.
    Redrawn after Vlad Khononov, <em>Learning Domain-Driven Design</em>,
    ch. 12.</figcaption>
</figure>

That is why the aggregate sticky note sits between the command and the event. On our
board below, the aggregate note sits on top of each command–event pair to save space. It
means the same thing: the command goes to the aggregate, and the aggregate produces the
event. **Aggregates pair commands with events.** An aggregate exposes one method per
command, and each method makes a particular event (or events) happen. This way the
system moves in a known, predefined direction. It also means that all directions should
be listed on the whiteboard.

*The name of the unit must be a noun.* Name it after the business concept whose rules it
protects (for example, *Order*), not after a database table. We place an aggregate on
every command–event pair, and the same aggregate name may repeat. This should not worry
us. On the contrary, it will help us in the next step of the EventStorming session.

Keep aggregates small. Most aggregates have one root entity plus some value objects. Some
have two or three entities. This is a heuristic, not a rule. The real test is the
invariant: put inside the aggregate only the data that its rules need. If it holds data
that no rule needs, or if many users often change it at the same time and conflict, the
aggregate should be split.

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/aggregate.jpg' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/aggregate.jpg' | relative_url }}"
         width="1428" height="1093" loading="lazy"
         alt="The full board with yellow aggregate notes (Order, User, Configuration,
              AuditLog) placed above their command and event pairs.">
  </a>
  <figcaption>Yellow aggregate notes on top of each command–event pair. Names may
    repeat.</figcaption>
</figure>

*The aggregate keeps the state, and this state must survive between commands.* To decide
on the next command, the aggregate must know what happened before. So, when we turn the
model into code, we persist every aggregate: either its current state, or the list of its
events (*event sourcing*). There is one *repository* per aggregate type. It loads the
whole aggregate by its ID and saves it in one transaction. A policy that must remember
earlier events (for example, "wait for both *Payment received* and *Stock reserved*")
also keeps state. In code, it becomes a *process manager* (also called a saga).

> **Modeling tip**
>
> The aggregate is responsible for keeping the invariants, so its state is always
> consistent with its data. Use the "No" rule to find these invariants. Do not ask *"What
> data is here?"* Instead, ask: *"Which rules must be checked together to say 'No' to a
> command?"* For example, if you cannot *Cancel order* without checking the *Shipping
> status*, and the answer must be correct at the moment of the click, then Order and
> Shipping belong in the same consistency boundary (aggregate). But first ask the experts:
> *"Is a short delay acceptable?"* Usually it is. Then keep them as separate aggregates. A
> policy reacts to *Order cancelled* and sends *Stop shipment*. If the parcel has already
> left, a compensating action follows, for example *Request return*.
{: .callout .callout--modeling}

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/system-bounded-contexts.svg' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/system-bounded-contexts.svg' | relative_url }}"
         width="1000" height="510" loading="lazy"
         alt="Three dashed boxes for the Ordering, Users, and Audit contexts. Each holds
              commands, one aggregate, and events. Pink policies between the boxes connect
              an event in one context to a command in another.">
  </a>
  <figcaption>The system split into bounded contexts. Policies connect them. Redrawn
    after Vlad Khononov, <em>Learning Domain-Driven Design</em>, ch. 12.</figcaption>
</figure>

### When It Is Hard to Tell Two Aggregates Apart

Ask these questions:

- What is the definition of the aggregate?
- How do we tell one from the other?
- **What is its identity?**
- Which events bring it into existence?

If an aggregate feels too big because it "changes too much data", it is a sign that you
are modeling **data** instead of **process**. Break it apart by asking: *"Can this command
succeed while the other part of the data is changing?"* If the answer is yes, they are
separate aggregates.

## Programmers Take Responsibility

Until now, the domain experts led the discussion. From here on, the programmers lead,
because we now draw the service boundaries. But the domain experts should stay in the
room. Only they can confirm that an aggregate protects the right business rules and that
a policy is correct. They also know where a word changes its meaning, and that is usually
where a bounded context ends. Their role changes from telling the story to checking our
design.

It is very important to save this state of the board, because we will need it when we add
new features or refactor!

## Drawing the Microservice Boundaries

A **bounded context** is the boundary inside which one model and one language are valid.
Inside a context, no term can be ambiguous. The same word can mean different things in
two contexts: a *Product* in the Catalog context (description, photos) is not the same as
a *Product* in the Warehouse context (weight, shelf). In code, a context is a relatively
standalone part of the system, with its own model and data. It is usually a separate
microservice, but not always. Here we must be creative and trust our instincts. Here are
some guidelines.

**The first and most important step is to remove the time factor.** Time no longer exists
for us, so the current order of the events (facts) does not matter. Delete the timeline
from the whiteboard.

**Then we merge all units with the same aggregate name into one!** But first check that
the name means the same thing in both places. If *Order* means one thing to sales and
another thing to shipping, these are two different models, and they belong in two
different bounded contexts. Since we use a digital whiteboard, the arrows move with the
notes. **The arrows show the communication channels, which are event-driven.**

The next step is to group aggregates whose names are not the same but *close*, because
they belong together. Good signs are: they use the same terms (the same definitions), many
policies connect them, they serve the same business capability, and the same experts know
them. A word that changes its meaning, or a different group of experts, is a strong sign
of a boundary.

Here we have to make two reservations:

1. **Load.** Ask: *"Could this service become a bottleneck?"* If one part of a context is
   called much more often than the rest, we can deploy that part as a separate service
   and scale it alone. This changes the deployment, not the model. It is still the same
   bounded context.
2. **Consistency.** One transaction changes only one aggregate. A rule that must be true
   *immediately* belongs inside one aggregate. Between aggregates, and always between
   contexts, we accept *eventual consistency*: an event is published, a policy reacts,
   and the next command runs in its own transaction. A service should never need a
   distributed transaction, or a synchronous call to another service, to save its own
   data.

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/bounded-context.jpg' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/bounded-context.jpg' | relative_url }}"
         width="1446" height="1079" loading="lazy"
         alt="The board without a timeline. Notes with the same aggregate name are merged
              and grouped into boundaries, with arrows between the groups.">
  </a>
  <figcaption>Without the timeline, notes with the same aggregate are merged into
    boundaries.</figcaption>
</figure>

> **Modeling tip**
>
> An aggregate often has a lifecycle, for example *Placed → Paid → Shipped*, or
> *Cancelled*. It is very common to implement it as a small *state machine* inside the
> aggregate. Each command is allowed only in some states, and the aggregate rejects it in
> the others. A long-running process that spans several aggregates or contexts has its own
> state machine in a *process manager* (saga). Both store their state through a
> repository.
{: .callout .callout--modeling}

### Relationships Between Contexts

The arrows between the groups show that contexts depend on each other. DDD gives names to
these relationships, and we draw them on a **context map**. The patterns describe how the
teams and their models relate, not how the data travels:

- **Shared Kernel:** two contexts share a small part of the model (and its code). Both
  teams must agree on every change to it.
- **Customer/Supplier:** the downstream team (customer) depends on the upstream team
  (supplier). The supplier takes the customer's needs into account when it plans changes.
- **Conformist:** the downstream team uses the upstream model as it is, because it cannot
  influence it and a translation is not worth the cost.
- **Anticorruption Layer:** the downstream team builds a translation layer, so that the
  upstream model does not leak into its own model.
- **Open Host Service:** the upstream context offers one well-defined API (protocol) for
  all its consumers, instead of a custom integration for each one.
- **Published Language:** a documented exchange format, for example a schema for events or
  messages. It is often used together with an Open Host Service.

The DDD literature also describes **Partnership** and **Separate Ways**. For more detail,
see [DDD Crew: Context Mapping](https://github.com/ddd-crew/context-mapping).

Each of these relationships can be implemented in two ways:

- **Synchronous (request/response):** one context calls another and waits for the answer,
  for example over REST. It is simple, but both sides must be up at the same time.
- **Asynchronous (events):** one context publishes a domain event, and the other reacts to
  it. This is what the policies between our groups show: an event in one context triggers
  a command in another. The policy usually lives in the context that reacts. The contexts
  are decoupled in time, and the data becomes *eventually consistent*.

Prefer asynchronous integration through policies. Use a synchronous call only when the
caller cannot continue without an immediate answer.

> **Note**
>
> An external system is not part of *our* bounded contexts. It has its own model and
> language, which we do not control. On the context map we draw it as a separate context,
> and we choose how to integrate with it: translate its model with an anticorruption
> layer, or accept its model as it is (Conformist).
{: .callout .callout--note}

## Adding New Features

When a new requirement arrives, we take a step back to the *aggregate phase*, where the
timeline still exists. We do not start from an empty board. We take the saved board, find
the part of the timeline that the new requirement changes, and run a short session on that
part with the right domain experts. Then we check the new events and commands against the
existing aggregates and boundaries. We try to keep the previous split into services, if
possible. Either a new microservice may appear, or new functionality may be added to an
existing aggregate. Only careful analysis will tell. Once we agree, we save the new board
as a document and continue with the code changes.

It is important to keep each stage of our work, because we can use it when we add new
features or revisit old decisions. It is also documentation for new team members.

## The Picture That Explains Everything

> All models are wrong, but some are useful.
>
> — George E. P. Box
{: .epigraph}

Brandolini calls this diagram "the picture that explains everything". It shows how all
the notes we used fit together.

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/event-storming.png' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/event-storming.png' | relative_url }}"
         width="1421" height="1099" loading="lazy"
         alt="A cycle: User issues a Command, which leads to a Domain Event. The event
              triggers a Policy, which issues a new Command. The event also updates a
              Read Model, which the User reads. An External System also produces Domain
              Events.">
  </a>
  <figcaption>All the building blocks in one cycle, after Alberto Brandolini's
    "picture that explains everything".</figcaption>
</figure>

In words, the picture says this. **The user** sends operation requests, called
**commands**, to the system. To decide what to send, the user uses information from the
system (from the UI or triggered automatically), shown as a **read model**, plus
information from the outside world and their own feelings and thoughts. A command might
cause a *state change* in the system, which produces **domain events**. These events can
trigger **policies**, which might issue new commands based on the information in those
events and on the system's state. **External systems** can also produce events, which we
translate into our own domain events. The domain events also update the read models, so
the user gets new information from the system, and the cycle repeats.

## Relation to User Stories

The
[IBM Event-Driven Reference Architecture](https://ibm-cloud-architecture.github.io/refarch-eda/methodology/event-storming/)
shows a simple way to turn the board into user stories. The commands and policies related
to events can be described as **user stories**, because actors issue commands and make
decisions. An actor can also be a system.

An event is the result, or outcome, of a user story. Events can be added to the acceptance
criteria of user stories, to verify that the event really happens.

<figure class="post-figure">
  <a href="{{ '/assets/images/event-storming/user-stories.png' | relative_url }}">
    <img src="{{ '/assets/images/event-storming/user-stories.png' | relative_url }}"
         width="720" height="270" loading="lazy"
         alt="Actor, commands, policies (whenever), data, and events mapped into two
              story templates: As an actor I want to … so that I can read …, and As an
              actor I want to … so that I can generate event ….">
  </a>
  <figcaption>From sticky notes to user stories. Image: IBM Event-Driven Reference
    Architecture.</figcaption>
</figure>

## Next Step: Domain-Driven Design

The most effective way to give structure to an event-driven system (one that we model with
events) is Domain-Driven Design. An EventStorming session models business process flows.
Domain-Driven Design gives us the discipline to turn those flows into a structured
software application. Here is a short example of what DDD says about aggregates. Vaughn
Vernon calls it a rule in *Implementing Domain-Driven Design*:
[reference other aggregates by identity](https://www.informit.com/articles/article.aspx?p=2020371&seqNum=4).
An aggregate keeps only the ID of another aggregate, never a direct object reference to
it. This keeps aggregates small and isolated, and it makes it clear that one transaction
changes only one aggregate. It also saves memory, because the other aggregate is not
loaded eagerly. When we need it, we load it by its ID from its repository.

## Resources

- [Introduction to EventStorming (video)](https://www.youtube.com/watch?v=QwOrbYTaDOY)
- [Bounded Context briefly (video)](https://www.youtube.com/watch?v=3-_0ZcI2SYc)
- [EventStorming and Reactive Systems](https://medium.com/redelastic/corporate-arts-crafts-modelling-reactive-systems-with-event-storming-73c6236f5dd7)
- [A more practical example (video)](https://www.youtube.com/watch?v=WvkBKvMnyuc)
- [Awesome EventStorming](https://github.com/mariuszgil/awesome-eventstorming)
