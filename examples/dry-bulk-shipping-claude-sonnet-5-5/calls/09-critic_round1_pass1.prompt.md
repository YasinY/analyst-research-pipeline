# critic/round1/pass1

## System prompt

You are the review agent of a research pipeline used by financial analysts. You act as an independent, adversarial reviewer. You did not write the briefing and you have no stake in it. Your job is to find what is wrong with it before an analyst relies on it.

You receive the briefing draft, the evidence groups it was allowed to use (each with a confidence level computed by the pipeline), and the list of sub-questions for which no adequate evidence was found.

Check the briefing for exactly these problems, in this order of importance:
1. UNSUPPORTED: a statement that is not backed by the evidence groups it cites, or that cites no group.
2. CONTRADICTS_EVIDENCE: a statement that says something different from what its cited groups say, including changed numbers, dates, or directions.
3. OVERSTATED_CERTAINTY: wording that adds certainty the cited groups do not carry. Calibration: a plain statement in the indicative mood ("the fleet grew 3.1 percent in 2025") is the correct register for MEDIUM and HIGH evidence and is not a finding. Flag only explicit certainty markers such as "clearly", "certainly", "undoubtedly", "confirmed", "will" for forecasts, or any LOW group presented as fact.
4. SMOOTHED_CONFLICT: two groups conflict, but the briefing presents one value, an average, or a resolution the evidence does not give. A sentence that names both values and both groups and calls the conflict open is the correct handling and is never a finding. Never ask the briefing to pick a base case or resolve a conflict; the evidence cannot do that.
5. MISSING_EVIDENCE: the analyst query clearly needs factual information that no evidence group covers and that is not already named as a gap. Formulate the missing piece as one research sub-question in suggestedQuestion and give 2 to 5 search keywords for it. Never use this type for wording, structure, or explanation problems; those are READABILITY.
6. READABILITY: a sentence a non-technical reader could not follow, or jargon without explanation.

Rules:
- Quote the exact text you object to in quotedText, copied from the briefing.
- Severity is MAJOR when an analyst could make a wrong decision because of it: a wrong or unsupported number, date, direction, or causal claim, or a hidden conflict. Disputes about emphasis words such as "main", "key", or "largest" are MINOR unless the evidence contradicts the emphasis.
- Do not invent evidence and do not propose new facts. You only compare the briefing with the evidence you were given.
- Do not report a gap that is already named as a gap in the briefing.
- Which groups a statement cites is not a finding as long as at least one cited group supports the statement. Redundant, incomplete, or differently chosen citation lists are never reported.
- If your explanation would begin by saying that something is handled correctly, it is not a finding. Leave it out.
- If you receive findings from your previous review, check those first: a finding that the revised draft has resolved is not reported again, and a finding that is still open is reported again with the same type and severity. New findings on a revised draft are limited to factual problems the revision introduced (UNSUPPORTED, CONTRADICTS_EVIDENCE, SMOOTHED_CONFLICT). A review of a revised draft never contains more findings than your previous review did. Do not raise the bar between passes.
- Report at most 8 findings, the most severe first. If the same problem affects several sentences, report it once and name the pattern in detail instead of repeating it per sentence.
- If you find nothing wrong, return an empty findings list. An empty list is a valid and welcome result.

Respond with a single JSON object and nothing else. No prose, no markdown fences. Use exactly this shape:

{
  "findings": [
    {
      "type": "UNSUPPORTED | CONTRADICTS_EVIDENCE | OVERSTATED_CERTAINTY | SMOOTHED_CONFLICT | MISSING_EVIDENCE | READABILITY",
      "severity": "MAJOR | MINOR",
      "quotedText": "exact text from the briefing",
      "detail": "one or two sentences explaining the problem",
      "groupIds": ["ids of the evidence groups involved, may be empty"],
      "suggestedKeywords": ["only for MISSING_EVIDENCE, otherwise empty"],
      "suggestedQuestion": "only for MISSING_EVIDENCE: the sub-question to research, as one sentence; otherwise null"
    }
  ]
}


## User prompt

Analyst query:

Give me an overview of the dry bulk shipping market and its main risk drivers.

Briefing draft under review:

Summary:
- Seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025, with iron ore and coal making up 55 percent of volumes and grain and soybeans 10 percent. (groups: g-q1-c1, g-q1-c2, g-q1-c3)
- Iron ore is the largest single cargo, and China took 71 percent of seaborne iron ore imports in 2025, which makes the market heavily dependent on Chinese demand. (groups: g-q1-c18, g-q1-c16)
- Distance carried grew faster than tonnes because voyages got longer, yet the freight index still averaged about 1,650 points in 2025, roughly 9 percent below 2024. (groups: g-q1-c7, g-q4-c1)
- Fleet supply is expected to grow by 2.0 to 2.5 percent in 2026 according to one broker, but the 2025 fleet growth figures from different sources disagree and are treated as unresolved. (groups: g-q3-c4)
- The main risks named for 2026 are weak Chinese steel demand, falling Chinese coal imports, the ramp-up of the Simandou iron ore mine in Guinea, grain trade shifts, port congestion, fuel prices, and route disruptions including a possible return to Suez routing. (groups: g-q5-c1, g-q5-c2, g-q2-c3, g-q5-c3, g-q5-c4, g-q5-c5, g-q5-c6, g-q5-c7, g-q2-c18)

Key facts:
- Seaborne iron ore trade reached 1.62 billion tonnes in 2025, up 1.3 percent, and iron ore is the largest dry bulk commodity, ahead of coal and grain. (groups: g-q1-c15, g-q1-c18)
- Australia supplied 56 percent and Brazil 24 percent of seaborne iron ore volumes in 2025. (groups: g-q1-c17)
- Tonne-miles (cargo weight multiplied by distance) rose 2.3 percent in 2025 while tonnes carried rose 1.1 percent, because average voyages got longer. The average laden voyage reached 5,200 nautical miles. (groups: g-q1-c7, g-q1-c9)
- Grain trade shifted towards South American exporters after a weak North American harvest. (groups: g-q1-c5)
- Capesize vessels make up 39 percent of dry bulk fleet capacity and Panamax 25 percent, with Supramax and Handysize making up the rest. (groups: g-q1-c13)
- Chinese seaborne coal imports fell about 6 percent in 2025, weighing on Pacific Panamax demand, and are expected to fall further in 2026. Indian coal imports grew 4 percent in 2025, partly offsetting the Chinese decline. (groups: g-q2-c2, g-q2-c3, g-q2-c4)
- Brokers see a return to Suez routing as the largest downside risk to 2026 freight rates, because it would release roughly 2 percent of fleet capacity. (groups: g-q2-c18)
- Demolition (scrapping of old ships) stayed low in 2025 because earnings remained above scrapping thresholds. Industry participants expect deliveries of new ships to stay elevated in 2026 and ease in 2027. (groups: g-q3-c8, g-q3-c14)
- Capesize earnings were the most volatile, swinging between 8,000 and 34,000 US dollars per day in 2025, while Panamax and Supramax were steadier at close to 12,000 US dollars per day. The composite freight index ended 2025 at 1,480 points. (groups: g-q4-c6, g-q4-c7, g-q4-c8)
- Nordhaven sees 2026 rates as supported by low fleet growth but capped by soft Chinese steel demand. Hanseatic Maritime Research names six risk drivers for 2026: Chinese steel and property demand, the Simandou ramp-up, grain flow shifts, port congestion, bunker fuel prices, and route disruption including Red Sea diversions. (groups: g-q3-c15, g-q5-c1, g-q5-c2, g-q5-c3, g-q5-c4, g-q5-c5, g-q5-c6, g-q5-c7)

Uncertainties:
- Sources disagree on how much Red Sea and Suez diversions added to 2025 tonne-mile growth: the Maritime Statistics Bureau says about 1 percentage point, while analysts estimate Cape of Good Hope diversions added 1 to 2 percent. This conflict is open and both estimates are weakly supported. (groups: g-q1-c8, g-q2-c17)
- Sources disagree on 2025 fleet growth: the Maritime Statistics Bureau reports 3.1 percent (to 1,020 million dwt), while Nordhaven reports 2.4 percent on a database that excludes long-term lay-up vessels. The conflict is open, so the 2026 fleet growth forecast of 2.0 to 2.5 percent rests on a base that is not agreed. (groups: g-q1-c10, g-q3-c1, g-q3-c4)
- Sources disagree on 2025 deliveries and removals: the Bureau reports deliveries of 31.6 million dwt and demolition of 3.9 million dwt, while Nordhaven reports 30.1 million dwt of deliveries and 5.5 million dwt of removals. This conflict is open. (groups: g-q1-c11, g-q3-c2)
- Sources disagree on the size of the orderbook (ships on order) at end-2025: the Bureau says 10.2 percent of the fleet, concentrated in Panamax and Supramax, while Nordhaven estimates 9.8 percent. This conflict is open. (groups: g-q1-c12, g-q3-c3)
- Nordhaven states that supply growth looks manageable relative to demand. This is a single-source, medium-confidence view. It sits alongside the unresolved disagreement on current fleet growth and the expectation of elevated deliveries in 2026. (groups: g-q3-c5, g-q3-c14)
- A blog claim that the Baltic Dry Index will double by 2027 and every Capesize will earn 60,000 dollars a day is unconfirmed and very weakly supported, coming from a low-quality source. (groups: g-q4-c11)
- A forum claim that an unpublished IMO rule will force half the dry bulk fleet into scrapping by 2027 is unconfirmed. Other posters say current CII and EEXI rules only require slow steaming or efficiency upgrades, and no poster cites an official document. The conflict is open and the evidence is very weak. (groups: g-q5-c9, g-q5-c10, g-q5-c11)

Follow-up questions:
- Which fleet growth figure for 2025 is more reliable, the Bureau's 3.1 percent or Nordhaven's 2.4 percent, and how much does excluding long-term lay-up vessels explain the difference?
- How much did Red Sea and Suez diversions really add to 2025 tonne-mile demand, and how quickly would demand fall if routing returns to Suez?
- How large is the orderbook and when will those ships be delivered, given the differing Bureau and Nordhaven estimates?
- How much could the Simandou ramp-up in Guinea change iron ore haul lengths and Capesize demand?
- What do official sources say about current CII and EEXI rules and their effect on fleet supply?

Evidence groups the briefing was allowed to use:

[g-q1-c1] confidence MEDIUM (0.60): Total seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c2] confidence MEDIUM (0.60): Iron ore and coal together made up 55 percent of seaborne dry bulk volumes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c3] confidence MEDIUM (0.60): Grain and soybeans made up 10 percent of seaborne dry bulk volumes in 2025, with minor bulks making up the remainder. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c16] confidence HIGH (0.90): China accounted for 71 percent of seaborne iron ore imports in 2025 (roughly 70 to 71 percent per the broker, unchanged from 2024). | 3 independent source(s), best tier A, newest 2026-04-09, conflict NONE
[g-q1-c5] confidence MEDIUM (0.60): Grain trade shifted towards South American exporters after a weak North American harvest. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c6] confidence MEDIUM (0.60): Tonne-mile demand grew faster than tonnes in 2025 because of longer voyage distances. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c7] confidence HIGH (0.75): Dry bulk tonne-mile demand rose 2.3 percent in 2025 while tonnes carried rose 1.1 percent, reflecting longer average voyage distances. | 2 independent source(s), best tier A, newest 2026-08-03, conflict NONE
[g-q1-c8] confidence LOW (0.30): Red Sea and Suez diversions accounted for an estimated 1 percentage point of 2025 tonne-mile growth. | 1 independent source(s), best tier A, newest 2026-08-03, conflict OPEN | conflicts with g-q2-c17: The Bureau attributes about 1 percentage point of 2025 tonne-mile growth to Red Sea/Suez diversions, while analysts estimate Cape diversions added 1 to 2 percent.
[g-q1-c9] confidence MEDIUM (0.60): Average laden voyage distance reached 5,200 nautical miles in 2025. | 1 independent source(s), best tier A, newest 2026-08-03, conflict NONE
[g-q1-c10] confidence LOW (0.30): The Maritime Statistics Bureau reports world dry bulk fleet growth of 3.1 percent in 2025 to 1,020 million dwt, the fastest since 2021. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c1: The Maritime Statistics Bureau reports 2025 dry bulk fleet growth of 3.1 percent while Nordhaven reports 2.4 percent for the same year.
[g-q1-c11] confidence LOW (0.30): The Maritime Statistics Bureau reports 2025 dry bulk deliveries of 31.6 million dwt and demolition of 3.9 million dwt. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c2: The Bureau reports 2025 deliveries of 31.6 million dwt and demolition of 3.9 million dwt, while Nordhaven reports 30.1 million dwt deliveries and 5.5 million dwt removals.
[g-q1-c12] confidence LOW (0.30): The Maritime Statistics Bureau puts the dry bulk orderbook at 10.2 percent of the fleet at end-2025, concentrated in Panamax and Supramax. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c3: The Bureau puts the dry bulk orderbook at 10.2 percent of the fleet while Nordhaven estimates 9.8 percent.
[g-q1-c13] confidence MEDIUM (0.60): Capesize vessels account for 39 percent of dry bulk fleet capacity and Panamax for 25 percent, with Supramax and Handysize the remainder. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g-q1-c14] confidence MEDIUM (0.60): The Maritime Statistics Bureau counts dry bulk vessels above 10,000 dwt. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g-q1-c15] confidence MEDIUM (0.60): Seaborne iron ore trade reached 1.62 billion tonnes in 2025, up 1.3 percent. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q1-c17] confidence MEDIUM (0.60): Australia supplied 56 percent and Brazil 24 percent of seaborne iron ore volumes in 2025. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q1-c18] confidence MEDIUM (0.60): Iron ore is the single largest dry bulk commodity, ahead of coal and grain. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q2-c2] confidence MEDIUM (0.40): Chinese seaborne coal imports fell about 6 percent in 2025, weighing on Pacific Panamax demand. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q2-c3] confidence MEDIUM (0.40): Chinese coal imports are expected to decline further in 2026. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q2-c4] confidence MEDIUM (0.40): Indian coal imports grew 4 percent in 2025, partly offsetting the Chinese decline. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q2-c17] confidence LOW (0.10): Analysts estimate Cape of Good Hope diversions added between 1 and 2 percent to dry bulk tonne-mile demand in 2025. | 1 independent source(s), best tier B, newest 2026-07-11, conflict OPEN | conflicts with g-q1-c8: The Bureau attributes about 1 percentage point of 2025 tonne-mile growth to Red Sea/Suez diversions, while analysts estimate Cape diversions added 1 to 2 percent.
[g-q2-c18] confidence MEDIUM (0.40): A return to Suez routing would release roughly 2 percent of fleet capacity, seen by brokers as the largest downside risk to 2026 freight rates. | 1 independent source(s), best tier B, newest 2026-07-11, conflict NONE
[g-q3-c1] confidence LOW (0.10): Nordhaven reports 2025 dry bulk fleet growth of 2.4 percent on its own database, which excludes long-term lay-up vessels. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g-q1-c10: The Maritime Statistics Bureau reports 2025 dry bulk fleet growth of 3.1 percent while Nordhaven reports 2.4 percent for the same year.
[g-q3-c2] confidence LOW (0.10): Nordhaven reports 2025 dry bulk deliveries of 30.1 million dwt and removals of 5.5 million dwt. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g-q1-c11: The Bureau reports 2025 deliveries of 31.6 million dwt and demolition of 3.9 million dwt, while Nordhaven reports 30.1 million dwt deliveries and 5.5 million dwt removals.
[g-q3-c3] confidence LOW (0.10): Nordhaven estimates the dry bulk orderbook at 9.8 percent of the fleet. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g-q1-c12: The Bureau puts the dry bulk orderbook at 10.2 percent of the fleet while Nordhaven estimates 9.8 percent.
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
[g-q4-c11] confidence LOW (0.15): The FreightMoonshot Blog claims the Baltic Dry Index will double by 2027 and every Capesize will earn 60,000 dollars a day. | 1 independent source(s), best tier C, newest 2026-05-02, conflict NONE
[g-q5-c1] confidence MEDIUM (0.40): Hanseatic Maritime Research identifies six main risk drivers for dry bulk earnings in 2026. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c2] confidence MEDIUM (0.40): Chinese steel demand and property construction is an identified risk driver for 2026. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c3] confidence MEDIUM (0.40): The Simandou iron ore ramp-up in Guinea, lengthening average hauls, is an identified risk driver. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c4] confidence MEDIUM (0.40): Shifts in grain trade flows between the Americas and Asia are an identified risk driver. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c5] confidence MEDIUM (0.40): Port congestion is an identified risk driver. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c6] confidence MEDIUM (0.40): Bunker fuel prices are an identified risk driver. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c7] confidence MEDIUM (0.40): Geopolitical disruption of routes, including Red Sea diversions, is an identified risk driver. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c9] confidence LOW (0.00): A forum poster claims an unpublished IMO rule will force half of the dry bulk fleet into scrapping by 2027. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g-q5-c10: One forum poster claims an IMO rule will force half the fleet into scrapping by 2027, while others state current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g-q5-c10] confidence LOW (0.00): Other forum posters state current CII and EEXI rules only require slow steaming or efficiency upgrades. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g-q5-c9: One forum poster claims an IMO rule will force half the fleet into scrapping by 2027, while others state current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g-q5-c11] confidence LOW (0.15): No poster in the forum thread cites an official document. | 1 independent source(s), best tier C, newest 2026-04-21, conflict NONE

Sub-questions already declared as gaps:

(none)

Findings from your previous review of this briefing, if any:

(first review of this briefing)

Review the briefing following the rules you were given.

