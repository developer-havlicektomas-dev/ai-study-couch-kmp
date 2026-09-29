# Core data

`NetworkConfig` validates and normalizes the configured server URL.
`HttpClientFactory` accepts a platform or mock engine, configures JSON and finite
timeouts, blocks HTTP in production, and disables redirects and body logging.
`safeCall<T>` translates HTTP, transport and decoding failures into domain results
while propagating coroutine cancellation. Android socket timeouts and Darwin
NSError failures are translated in platform source sets.

The application boundary supplies engines and owns reusable client instances;
coreDataModule provides the reusable client to Koin. This module does not create
clients per request or implement feature DTOs. See the root README for development URLs and production build settings.
