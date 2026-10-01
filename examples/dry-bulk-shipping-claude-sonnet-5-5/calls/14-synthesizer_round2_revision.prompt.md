# synthesizer/round2/revision

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

The analyst wants a structured overview of the dry bulk shipping market (its scope, demand and supply fundamentals, freight rate behavior, and outlook) with emphasis on the factors that create risk for market participants.

Evidence eligible for key facts:

[g1] confidence MEDIUM (0.60): Total seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g2] confidence MEDIUM (0.60): Iron ore and coal together made up 55 percent of seaborne dry bulk volumes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g3] confidence MEDIUM (0.60): Grain and soybeans made up 10 percent of seaborne dry bulk volumes in 2025, with minor bulks making up the remainder. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g5] confidence MEDIUM (0.60): Capesize vessels account for 39 percent and Panamax for 25 percent of dry bulk fleet capacity, with Supramax and Handysize making up the remainder. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g7] confidence MEDIUM (0.60): The Maritime Statistics Bureau counts dry bulk vessels above 10,000 dwt. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g8] confidence MEDIUM (0.60): Seaborne iron ore trade reached 1.62 billion tonnes in 2025, up 1.3 percent. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g9] confidence MEDIUM (0.60): Iron ore remains the single largest dry bulk commodity, ahead of coal and grain. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g10] confidence HIGH (0.90): China accounted for about 71 percent of seaborne iron ore imports in 2025 (1.15 billion tonnes). | 3 independent source(s), best tier A, newest 2026-04-09, conflict NONE
[g11] confidence MEDIUM (0.40): Chinese seaborne coal imports fell about 6 percent in 2025, weighing on Panamax demand in the Pacific. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g12] confidence MEDIUM (0.40): Nordhaven Shipbrokers expects Chinese coal imports to decline further in 2026. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g13] confidence MEDIUM (0.40): Indian coal imports grew 4 percent in 2025 and partly offset the Chinese decline. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g14] confidence MEDIUM (0.60): Grain trade shifted towards South American exporters after a weak North American harvest. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g15] confidence HIGH (0.90): Tonne-mile demand grew faster than tonnes in 2025 because of longer voyage distances: 2.3 percent versus 1.1 percent (roughly double). | 3 independent source(s), best tier A, newest 2026-08-03, conflict NONE
[g16] confidence MEDIUM (0.40): Hanseatic Maritime Research identifies six main risk drivers for dry bulk earnings in 2026: Chinese steel demand and property, Simandou ramp-up, grain flow shifts, port congestion, bunker prices, and geopolitical route disruption including Red Sea diversions. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g17] confidence MEDIUM (0.40): The Simandou iron ore ramp-up in Guinea lengthens average haul distances. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g18] confidence MEDIUM (0.60): Australia supplied 56 percent and Brazil 24 percent of seaborne iron ore volumes in 2025. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g19] confidence MEDIUM (0.60): Red Sea and Suez diversions accounted for an estimated 1 percentage point of 2025 tonne-mile growth. | 1 independent source(s), best tier A, newest 2026-08-03, conflict NONE
[g20] confidence MEDIUM (0.60): Average laden voyage distance reached 5,200 nautical miles in 2025. | 1 independent source(s), best tier A, newest 2026-08-03, conflict NONE
[g26] confidence MEDIUM (0.40): Nordhaven expects net dry bulk fleet growth of 2.0 to 2.5 percent in 2026 as yard slots stay tight and newbuilding prices high. | 1 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g27] confidence MEDIUM (0.40): Nordhaven states dry bulk supply growth looks manageable relative to demand. | 1 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g28] confidence MEDIUM (0.40): Industry participants expect dry bulk deliveries to stay elevated in 2026 before easing in 2027. | 1 independent source(s), best tier B, newest 2026-02-12, conflict NONE
[g29] confidence HIGH (0.75): The dry bulk freight index averaged 1,650 points in 2025, about 9 percent below 2024. | 2 independent source(s), best tier A, newest 2026-01-22, conflict NONE
[g30] confidence MEDIUM (0.40): Capesize volatility in 2025 was driven by Brazilian iron ore timing and Chinese restocking. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g31] confidence MEDIUM (0.40): One-year time charter rates for a modern Kamsarmax averaged 13,500 US dollars per day in 2025. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g32] confidence MEDIUM (0.40): Nordhaven sees 2026 rates supported by low fleet growth but capped by soft Chinese steel demand. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g33] confidence MEDIUM (0.60): Capesize earnings were the most volatile segment in 2025, swinging between 8,000 and 34,000 US dollars per day. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g34] confidence MEDIUM (0.60): Panamax and Supramax earnings averaged close to 12,000 US dollars per day in 2025. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g35] confidence MEDIUM (0.60): The dry bulk freight index ended 2025 at 1,480 points. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g36] confidence MEDIUM (0.40): Hanseatic lists Red Sea diversions as an example of geopolitical route disruption affecting dry bulk earnings. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE

Weak evidence (may only appear in uncertainties):

[g4] confidence LOW (0.30): The world dry bulk fleet grew 3.1 percent in 2025 to 1,020 million dwt (the fastest expansion since 2021). | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g23: The Maritime Statistics Bureau puts 2025 dry bulk fleet growth at 3.1 percent while Nordhaven's database gives 2.4 percent.
[g6] confidence LOW (0.30): The dry bulk orderbook stood at 10.2 percent of the fleet at the end of 2025, concentrated in the Panamax and Supramax segments. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g25: The dry bulk orderbook is 10.2 percent of the fleet per the Maritime Statistics Bureau but 9.8 percent per Nordhaven.
[g21] confidence LOW (0.30): Dry bulk deliveries reached 31.6 million dwt in 2025. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g24: 2025 dry bulk deliveries are 31.6 million dwt per the Maritime Statistics Bureau but 30.1 million dwt per Nordhaven.
[g22] confidence LOW (0.30): Dry bulk demolition stayed low at 3.9 million dwt in 2025 because earnings stayed above scrapping thresholds. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g24: 2025 dry bulk removals/demolition are 3.9 million dwt per the Maritime Statistics Bureau but 5.5 million dwt per Nordhaven.
[g23] confidence LOW (0.10): On Nordhaven's vessel database the dry bulk fleet expanded 2.4 percent in 2025. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g4: The Maritime Statistics Bureau puts 2025 dry bulk fleet growth at 3.1 percent while Nordhaven's database gives 2.4 percent.
[g24] confidence LOW (0.10): Nordhaven reports dry bulk deliveries of 30.1 million dwt and removals of 5.5 million dwt in 2025. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g21, g22: 2025 dry bulk deliveries are 31.6 million dwt per the Maritime Statistics Bureau but 30.1 million dwt per Nordhaven.
[g25] confidence LOW (0.10): Nordhaven estimates the dry bulk orderbook at 9.8 percent of the fleet. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g6: The dry bulk orderbook is 10.2 percent of the fleet per the Maritime Statistics Bureau but 9.8 percent per Nordhaven.
[g37] confidence LOW (0.00): A forum poster claims an unpublished IMO rule will force half of the dry bulk fleet into scrapping by 2027 due to carbon intensity ratings. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g38: A forum poster claims an IMO rule will force half the fleet into scrapping by 2027, while other posters say current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g38] confidence LOW (0.00): Other forum posters state current CII and EEXI rules only require slow steaming or efficiency upgrades. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g37: A forum poster claims an IMO rule will force half the fleet into scrapping by 2027, while other posters say current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g39] confidence LOW (0.15): No poster in the thread cites an official document for the claim about the IMO rule. | 1 independent source(s), best tier C, newest 2026-04-21, conflict NONE

Gaps (sub-questions without adequate evidence):

[q6] What do independent forecasters project for dry bulk trade volume and tonne-mile demand growth in 2026?

Revision context:

Previous draft:
Summary:
- Seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025, with iron ore and coal making up 55 percent of volumes and grain and soybeans 10 percent. (groups: g1, g2, g3)
- Tonne-mile demand (cargo moved multiplied by distance) grew 2.3 percent, roughly double the growth in tonnes, because voyages got longer; Red Sea and Suez diversions are estimated to have added about 1 percentage point. (groups: g15, g19, g20)
- Freight rates were weaker overall: the dry bulk freight index averaged 1,650 points in 2025, about 9 percent below 2024, and Capesize earnings were the most volatile. (groups: g29, g33)
- On supply, one lower-tier source (Nordhaven) expects net fleet growth of 2.0 to 2.5 percent in 2026 and calls supply growth manageable, while industry participants expect deliveries to stay elevated in 2026 before easing in 2027; the 2025 fleet-growth base itself is disputed between sources, so this outlook is only weakly supported. (groups: g26, g27, g28, g4, g23)
- Hanseatic Maritime Research, a single lower-tier source, lists six main risk drivers for 2026 earnings: Chinese steel demand and property, Simandou ramp-up, grain flow shifts, port congestion, bunker (ship fuel) prices and geopolitical route disruption including Red Sea diversions. (groups: g16, g36)

Key facts:
- Seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025. (groups: g1)
- Iron ore and coal together made up 55 percent of seaborne dry bulk volumes in 2025, and grain and soybeans made up 10 percent, with minor bulks making up the remainder. (groups: g2, g3)
- Iron ore is the single largest dry bulk commodity; seaborne iron ore trade reached 1.62 billion tonnes in 2025, up 1.3 percent. (groups: g8, g9)
- China took about 71 percent of seaborne iron ore imports in 2025 (1.15 billion tonnes), while Australia supplied 56 percent and Brazil 24 percent of seaborne iron ore volumes. (groups: g10, g18)
- Capesize vessels account for 39 percent and Panamax vessels 25 percent of dry bulk fleet capacity, with Supramax and Handysize making up the remainder. (groups: g5)
- Tonne-mile demand grew 2.3 percent in 2025 versus 1.1 percent for tonnes, because of longer voyage distances; Red Sea and Suez diversions accounted for an estimated 1 percentage point of that growth, and average laden voyage distance reached 5,200 nautical miles. (groups: g15, g19, g20)
- The dry bulk freight index averaged 1,650 points in 2025, about 9 percent below 2024, and ended the year at 1,480 points. (groups: g29, g35)
- Capesize earnings swung between 8,000 and 34,000 US dollars per day in 2025, while Panamax and Supamax earnings averaged close to 12,000 US dollars per day. (groups: g33, g34)
- Grain trade shifted towards South American exporters after a weak North American harvest. (groups: g14)

Uncertainties:
- Fleet growth in 2025 is disputed: the Maritime Statistics Bureau says 3.1 percent, while Nordhaven's database says 2.4 percent. Both figures are weakly supported and neither can be treated as established. (groups: g4, g23)
- The orderbook (ships already ordered but not yet delivered) is put at 10.2 percent of the fleet by the Maritime Statistics Bureau and 9.8 percent by Nordhaven. The conflict is open and both figures are weakly supported. (groups: g6, g25)
- 2025 deliveries are 31.6 million dwt per the Maritime Statistics Bureau but 30.1 million dwt per Nordhaven. The conflict is open and both figures are weakly supported. (groups: g21, g24)
- 2025 demolition (ships scrapped) is 3.9 million dwt per the Maritime Statistics Bureau but 5.5 million dwt per Nordhaven. The conflict is open and both figures are weakly supported. (groups: g22, g24)
- A forum poster claims an unpublished IMO rule will force half the dry bulk fleet into scrapping by 2027, while other posters say current CII and EEXI rules only require slow steaming or efficiency upgrades. This is unconfirmed, and no poster cites an official document. (groups: g37, g38, g39)
- The 2026 supply outlook rests on single lower-tier sources: Nordhaven expects net fleet growth of 2.0 to 2.5 percent and calls supply growth manageable, and industry participants expect elevated deliveries in 2026 before easing in 2027. The disputed 2025 fleet-growth base makes this outlook only weakly supported. (groups: g26, g27, g28, g4, g23)
- The risk-driver list comes from one lower-tier source (Hanseatic), and its claim that the Simandou ramp-up in Guinea lengthens average haul distances is likewise a single-source view. It is a 2026 risk driver, so it is not part of the 2025 voyage-distance figures. (groups: g16, g17)
- The coal-demand findings (Chinese imports down about 6 percent in 2025, a further decline expected in 2026 per Nordhaven, Indian imports up 4 percent) each come from one lower-tier source, so they are only moderately supported. (groups: g11, g12, g13)
- No adequate evidence was found on what independent forecasters project for dry bulk trade volume and tonne-mile demand growth in 2026, so the demand-side outlook is a gap. (groups: )

Follow-up questions:
- What do independent forecasters project for dry bulk trade volume and tonne-mile demand growth in 2026?
- Which figures for 2025 fleet growth, deliveries and demolition are correct, and why do the Maritime Statistics Bureau and Nordhaven differ?
- Is the orderbook 10.2 percent or 9.8 percent of the fleet, and how much of it is due for delivery in 2026 versus 2027?
- Is there any official IMO document supporting a mass-scrapping requirement, or do only CII and EEXI efficiency rules apply?
- How much will the Simandou ramp-up add to iron ore volumes and haul distances in 2026?

Review findings to resolve:
- MINOR UNSUPPORTED on "Tonne-mile demand (cargo moved multiplied by distance) grew 2.3 percent, roughly double the growth in tonnes, because voyages got longer; Red Sea and Suez diversions are estimated to have added about 1 percentage point.": The summary bullet cites g15, g19 and g20, but the 'because voyages got longer' causal claim is carried by g15 and the 5,200 nm figure in g20 is not used. This is a weak citation fit rather than a wrong claim. The core numbers do match g15 and g19.
- MINOR MISSING_EVIDENCE on "Hanseatic Maritime Research, a single lower-tier source, lists six main risk drivers for 2026 earnings": The query asks for the main risk drivers, but the briefing only reports one source's list and offers no evidence on how large or likely each driver is (e.g., the Chinese steel and property outlook, or bunker price levels). The analyst cannot weigh the drivers against each other. This is not named as a gap.

Write the briefing following the rules you were given.

