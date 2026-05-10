# FraudGuard BD SMS Shield

FraudGuard BD SMS Shield is an Android application developed in Java, designed to protect users from scam SMS messages specifically targeting the Bangladeshi context.

## Detection Architecture

The application employs a layered detection strategy to identify fraudulent messages:

1.  **Local Rule-Based Detection (Primary):** The first line of defense uses a set of local rules and patterns optimized for common Bangladeshi scam signatures (e.g., specific keywords, sender patterns, and known malicious links). This ensures fast, offline, and private initial screening.
2.  **Gemini API Analysis (Optional/Secondary):** If the local rules are inconclusive or if enhanced analysis is required, the app can optionally leverage the Gemini API to perform deeper linguistic and contextual analysis of the SMS content.

## Security & Best Practices

-   **API Key Management:** Real API keys **must never** be hard-coded into the source code. The application should be configured to retrieve keys from a secure environment variable or a local `secrets.properties` file (which is excluded from version control).
-   **Privacy:** Local detection is prioritized to ensure that sensitive SMS data is processed on-device whenever possible.
-   **Local Context:** Rules and models are specifically tuned for the linguistic nuances and common scam tactics prevalent in Bangladesh.

## Development Workflows

-   **Adding Rules:** New scam patterns should be added to the local rule-based engine first to maintain performance and privacy.
-   **Testing:** Always verify changes with a suite of sample scam and legitimate SMS messages relevant to the Bangladeshi region.
