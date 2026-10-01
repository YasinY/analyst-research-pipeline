# synthesizer/round1

## System prompt

You are the synthesis agent of a research pipeline used by financial analysts.

Your only job is to write a short briefing for a non-technical analyst from the evidence you are given. The evidence has already been collected, compared, and scored by other parts of the pipeline. You do not add knowledge of your own, you do not change the confidence scores, and you do not resolve conflicts that the evidence leaves open.

Rules:
- Every statement in summary, keyFacts, and uncertainties must cite the ids of the evidence groups it is based on, in groupIds. A statement without a groupId will be removed before the briefing is shown. Put the ids only in groupIds, never inside the text itself.
- The summary answers the analyst query directly in 3 to 5 sentences and touches every sub-question that has evidence, including the risk or outlook dimension if the query asks for it.
- Write 5 to 10 key facts that together cover every sub-question with eligible evidence. Qualitative findings from eligible groups, such as a named risk driver, are key facts too, not uncertainties.
- Uncertainties are reserved for three things: open conflicts between groups, weak evidence, and gaps. Do not describe two compatible facts as if they disagreed.
- Key facts may only be built from evidence groups listed under "Evidence eligible for key facts". Each key fact should rest on one group, or on several groups that agree.
- Groups listed under "Weak evidence" may only appear in uncertainties, phrased as unconfirmed or weakly supported, never as fact.
- Where two groups conflict, say so explicitly, give both values, and name both groups. Never average, pick a side, or smooth the conflict away.
- Evidence groups are formed per sub-question, so the same quantity can appear in one group without a conflict and in another group with an open conflict. Treat such a quantity as conflicted everywhere: do not state it as a key fact from the unconflicted group.
- Every sub-question listed under "Gaps" must appear in uncertainties with a plain statement that no adequate evidence was found for it.
- Use only numbers, dates, and names that appear in the evidence. Do not round, extrapolate, or add context from memory.
- Never write about the briefing itself, its sections, its evidence groups or what is "shown above"; write only about the subject matter.
- Write in plain English for a reader without technical or domain background. Short sentences. No jargon without a short explanation.
- Suggest 3 to 5 follow-up questions an analyst could pursue next, focused on the gaps and conflicts.
- If you are given a previous draft and review findings, produce a corrected draft that resolves every finding. Keep what was not criticised.

Respond with a single JSON object and nothing else. No prose, no markdown fences. Use exactly this shape:

{
  "summary": [
    { "text": "one sentence", "groupIds": ["q1-g1"] }
  ],
  "keyFacts": [
    { "text": "one factual sentence", "groupIds": ["q1-g1"] }
  ],
  "uncertainties": [
    { "text": "one sentence naming a conflict, a weak finding, or a gap", "groupIds": ["q2-g1", "q2-g2"] }
  ],
  "followUpQuestions": [
    "a question an analyst could pursue next"
  ]
}

For a gap with no evidence group, use an empty groupIds list.


## User prompt

Analyst query:

Give me an overview of the dry bulk shipping market and its main risk drivers.

How the planner understood the query:

The analyst wants a broad overview of the dry bulk shipping market, covering its structure and current conditions, together with the principal factors that create risk for market participants.

Evidence eligible for key facts:

[g-q1-c1] confidence MEDIUM (0.60): Total seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c2] confidence MEDIUM (0.60): Iron ore and coal together made up 55 percent of seaborne dry bulk volumes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c3] confidence MEDIUM (0.60): Grain and soybeans made up 10 percent of seaborne dry bulk volumes in 2025, with minor bulks making up the remainder. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c16] confidence HIGH (0.90): China accounted for 71 percent of seaborne iron ore imports in 2025 (roughly 70 to 71 percent per the broker, unchanged from 2024). | 3 independent source(s), best tier A, newest 2026-04-09, conflict NONE
[g-q1-c5] confidence MEDIUM (0.60): Grain trade shifted towards South American exporters after a weak North American harvest. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c6] confidence MEDIUM (0.60): Tonne-mile demand grew faster than tonnes in 2025 because of longer voyage distances. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c7] confidence HIGH (0.75): Dry bulk tonne-mile demand rose 2.3 percent in 2025 while tonnes carried rose 1.1 percent, reflecting longer average voyage distances. | 2 independent source(s), best tier A, newest 2026-08-03, conflict NONE
[g-q1-c9] confidence MEDIUM (0.60): Average laden voyage distance reached 5,200 nautical miles in 2025. | 1 independent source(s), best tier A, newest 2026-08-03, conflict NONE
[g-q1-c13] confidence MEDIUM (0.60): Capesize vessels account for 39 percent of dry bulk fleet capacity and Panamax for 25 percent, with Supramax and Handysize the remainder. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g-q1-c14] confidence MEDIUM (0.60): The Maritime Statistics Bureau counts dry bulk vessels above 10,000 dwt. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g-q1-c15] confidence MEDIUM (0.60): Seaborne iron ore trade reached 1.62 billion tonnes in 2025, up 1.3 percent. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q1-c17] confidence MEDIUM (0.60): Australia supplied 56 percent and Brazil 24 percent of seaborne iron ore volumes in 2025. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q1-c18] confidence MEDIUM (0.60): Iron ore is the single largest dry bulk commodity, ahead of coal and grain. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q2-c2] confidence MEDIUM (0.40): Chinese seaborne coal imports fell about 6 percent in 2025, weighing on Pacific Panamax demand. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q2-c3] confidence MEDIUM (0.40): Chinese coal imports are expected to decline further in 2026. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q2-c4] confidence MEDIUM (0.40): Indian coal imports grew 4 percent in 2025, partly offsetting the Chinese decline. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q2-c18] confidence MEDIUM (0.40): A return to Suez routing would release roughly 2 percent of fleet capacity, seen by brokers as the largest downside risk to 2026 freight rates. | 1 independent source(s), best tier B, newest 2026-07-11, conflict NONE
[g-q3-c4] confidence MEDIUM (0.40): Nordhaven expects net dry bulk fleet growth of 2.0 to 2.5 percent in 2026 given tight yard slots and high newbuilding prices. | 1 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g-q3-c5] confidence MEDIUM (0.40): Nordhaven states that supply growth looks manageable relative to demand. | 1 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g-q3-c8] confidence MEDIUM (0.60): Demolition stayed low in 2025 because earnings remained above scrapping thresholds. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g-q3-c14] confidence MEDIUM (0.40): Industry participants expect dry bulk deliveries to stay elevated in 2026 before easing in 2027. | 1 independent source(s), best tier B, newest 2026-02-12, conflict NONE
[g-q3-c15] confidence MEDIUM (0.40): Nordhaven sees 2026 dry bulk rates as supported by low fleet growth but capped by soft Chinese steel demand. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g-q4-c1] confidence HIGH (0.75): The dry bulk freight index averaged about 1,650 points in 2025, roughly 9 percent below 2024. | 2 independent source(s), best tier A, newest 2026-01-22, conflict NONE
[g-q4-c2] confidence MEDIUM (0.40): Capesize volatility in 2025 was driven by Brazilian iron ore timing and Chinese restocking. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g-q4-c3] confidence MEDIUM (0.40): One-year time charter rates for a modern Kamsarmax averaged 13,500 US dollars per day. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g-q4-c6] confidence MEDIUM (0.60): Capesize earnings were the most volatile segment, swinging between 8,000 and 34,000 US dollars per day in 2025. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g-q4-c7] confidence MEDIUM (0.60): Panamax and Supramax earnings were steadier and averaged close to 12,000 US dollars per day in 2025. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g-q4-c8] confidence MEDIUM (0.60): The composite dry bulk freight index ended 2025 at 1,480 points. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g-q5-c1] confidence MEDIUM (0.40): Hanseatic Maritime Research identifies six main risk drivers for dry bulk earnings in 2026. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c2] confidence MEDIUM (0.40): Chinese steel demand and property construction is an identified risk driver for 2026. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c3] confidence MEDIUM (0.40): The Simandou iron ore ramp-up in Guinea, lengthening average hauls, is an identified risk driver. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c4] confidence MEDIUM (0.40): Shifts in grain trade flows between the Americas and Asia are an identified risk driver. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c5] confidence MEDIUM (0.40): Port congestion is an identified risk driver. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c6] confidence MEDIUM (0.40): Bunker fuel prices are an identified risk driver. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c7] confidence MEDIUM (0.40): Geopolitical disruption of routes, including Red Sea diversions, is an identified risk driver. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE

Weak evidence (may only appear in uncertainties):

[g-q1-c8] confidence LOW (0.30): Red Sea and Suez diversions accounted for an estimated 1 percentage point of 2025 tonne-mile growth. | 1 independent source(s), best tier A, newest 2026-08-03, conflict OPEN | conflicts with g-q2-c17: The Bureau attributes about 1 percentage point of 2025 tonne-mile growth to Red Sea/Suez diversions, while analysts estimate Cape diversions added 1 to 2 percent.
[g-q1-c10] confidence LOW (0.30): The Maritime Statistics Bureau reports world dry bulk fleet growth of 3.1 percent in 2025 to 1,020 million dwt, the fastest since 2021. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c1: The Maritime Statistics Bureau reports 2025 dry bulk fleet growth of 3.1 percent while Nordhaven reports 2.4 percent for the same year.
[g-q1-c11] confidence LOW (0.30): The Maritime Statistics Bureau reports 2025 dry bulk deliveries of 31.6 million dwt and demolition of 3.9 million dwt. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c2: The Bureau reports 2025 deliveries of 31.6 million dwt and demolition of 3.9 million dwt, while Nordhaven reports 30.1 million dwt deliveries and 5.5 million dwt removals.
[g-q1-c12] confidence LOW (0.30): The Maritime Statistics Bureau puts the dry bulk orderbook at 10.2 percent of the fleet at end-2025, concentrated in Panamax and Supramax. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c3: The Bureau puts the dry bulk orderbook at 10.2 percent of the fleet while Nordhaven estimates 9.8 percent.
[g-q2-c17] confidence LOW (0.10): Analysts estimate Cape of Good Hope diversions added between 1 and 2 percent to dry bulk tonne-mile demand in 2025. | 1 independent source(s), best tier B, newest 2026-07-11, conflict OPEN | conflicts with g-q1-c8: The Bureau attributes about 1 percentage point of 2025 tonne-mile growth to Red Sea/Suez diversions, while analysts estimate Cape diversions added 1 to 2 percent.
[g-q3-c1] confidence LOW (0.10): Nordhaven reports 2025 dry bulk fleet growth of 2.4 percent on its own database, which excludes long-term lay-up vessels. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g-q1-c10: The Maritime Statistics Bureau reports 2025 dry bulk fleet growth of 3.1 percent while Nordhaven reports 2.4 percent for the same year.
[g-q3-c2] confidence LOW (0.10): Nordhaven reports 2025 dry bulk deliveries of 30.1 million dwt and removals of 5.5 million dwt. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g-q1-c11: The Bureau reports 2025 deliveries of 31.6 million dwt and demolition of 3.9 million dwt, while Nordhaven reports 30.1 million dwt deliveries and 5.5 million dwt removals.
[g-q3-c3] confidence LOW (0.10): Nordhaven estimates the dry bulk orderbook at 9.8 percent of the fleet. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g-q1-c12: The Bureau puts the dry bulk orderbook at 10.2 percent of the fleet while Nordhaven estimates 9.8 percent.
[g-q4-c11] confidence LOW (0.15): The FreightMoonshot Blog claims the Baltic Dry Index will double by 2027 and every Capesize will earn 60,000 dollars a day. | 1 independent source(s), best tier C, newest 2026-05-02, conflict NONE
[g-q5-c9] confidence LOW (0.00): A forum poster claims an unpublished IMO rule will force half of the dry bulk fleet into scrapping by 2027. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g-q5-c10: One forum poster claims an IMO rule will force half the fleet into scrapping by 2027, while others state current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g-q5-c10] confidence LOW (0.00): Other forum posters state current CII and EEXI rules only require slow steaming or efficiency upgrades. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g-q5-c9: One forum poster claims an IMO rule will force half the fleet into scrapping by 2027, while others state current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g-q5-c11] confidence LOW (0.15): No poster in the forum thread cites an official document. | 1 independent source(s), best tier C, newest 2026-04-21, conflict NONE

<!-- cache-boundary -->

Gaps (sub-questions without adequate evidence):

(none)

Revision context:

This is the first draft. There is no previous draft to revise.

Write the briefing following the rules you were given.

