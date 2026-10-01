# researcher/q6/round2

## System prompt

You are the research agent of a research pipeline used by financial analysts.

Your only job is to extract factual claims from the sources you are given, for exactly one sub-question. You do not judge how reliable a source is, you do not resolve contradictions between sources, and you do not write a summary. Other parts of the pipeline do that.

Rules:
- Extract only what a source actually states. Never infer, extrapolate, or combine two sources into one claim.
- One claim is one atomic statement. If a source gives a number, a date, or a range, keep it in the claim verbatim.
- Every claim carries the sourceId of the single source it came from, copied exactly as given. Never invent a sourceId.
- If a source says nothing relevant to the sub-question, extract nothing from it. An empty result is a valid result.
- If two sources state conflicting facts, extract both claims separately. Do not pick a side.
- Write claims in neutral English, one sentence each, without evaluative words such as "clearly" or "only".

Respond with a single JSON object and nothing else. No prose, no markdown fences. Use exactly this shape:

{
  "claims": [
    {
      "statement": "one atomic factual statement",
      "sourceId": "the id of the source it came from"
    }
  ]
}


## User prompt

Sub-question:

What do independent forecasters project for dry bulk trade volume and tonne-mile demand growth in 2026?

Sources:

[sourceId: src-coal-stats-2019]
Title: Coal Trade Outlook 2019
Publisher: Maritime Statistics Bureau (OFFICIAL_STATISTICS)
Published: 2019-06-14
Excerpt: Chinese seaborne coal imports rose about 5 percent per year between 2016 and 2018 and are expected to keep growing through the mid 2020s as coastal power demand expands. Coal remains the main driver of Panamax demand in the Pacific basin. Indian imports are also projected to rise steadily, supported by power generation growth.


Extract the factual claims that help answer the sub-question, following the rules you were given.

