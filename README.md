This proof of concept project is meant to show a way to decouple domain logic from actor but still be able to use facilities
that come with it (sending messages to other actors, persistence, etc.).

It uses free monads to implement domain logic in a pure way which is then interpreted in a context of actor.

The domain is an event-sourced aggregate root: `Game`.
It handles `GameCommands` with `handleCommand` which in the end gives a list of `GameEvent`s and a `CommandResult` (either `CommandSuccess` or `CommandError`).
`Game` state can only be changed by applying events using `handleEvent`.

This example project addresses following CQRS/ES use case:

1. Player wants to join a game (sends a command)
2. Game needs to contact external service (asynchronously) to check whether player is allowed to join
3. Game generates suitable event(s) or reply to the sender with an error

Notice that no logic associated with external service communication is implemented in `Game` class.
It just knows a set of operations it can use and not worry about how and where are they implemented.
