# Tutor presentation

KMP-05 adds the State/Action/Event contract and lifecycle-scoped TutorViewModel,
with saved input, validation, one active request, current-input retry, typed error
state, and quiz reset. TutorRoot resolves the ViewModel via Koin and observes state
and events with lifecycle awareness. TutorScreen receives only state/actions.

The minimal question/submit surface makes this wiring runnable. KMP-06–10 will add
mode/level controls, full responses, detailed localized errors and accessibility
previews. No networking or DTO mapping runs in composables.
