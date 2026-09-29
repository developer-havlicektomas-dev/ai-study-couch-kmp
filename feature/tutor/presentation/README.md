# Tutor presentation

KMP-05 adds the State/Action/Event contract and lifecycle-scoped TutorViewModel,
with saved input, validation, one active request, current-input retry, typed error
state, and quiz reset. TutorRoot resolves the ViewModel via Koin and observes state
and events with lifecycle awareness. TutorScreen receives only state/actions.

KMP-06 adds the multiline form, wrapping mode/level controls, trimmed Unicode
length feedback, near-limit counter and accessible loading label. Form previews
cover default, invalid, loading and near-limit states. Full response layouts,
detailed errors and broader accessibility checks remain KMP-07–10. No networking or DTO mapping runs in composables.
