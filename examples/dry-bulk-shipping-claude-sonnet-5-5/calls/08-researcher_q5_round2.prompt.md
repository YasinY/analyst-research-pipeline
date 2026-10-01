# researcher/q5/round2

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

What are the principal regulatory, geopolitical, and macroeconomic risks facing the dry bulk market, including IMO emissions rules, trade disputes, and shipping route disruptions?

Sources:

[sourceId: src-forum-regulation-2026]
Title: Secret IMO rule will force half the bulk fleet to scrap in 2027 (thread)
Publisher: ShipTalk Forum (FORUM)
Published: 2026-04-21
Excerpt: A poster claims that an unpublished IMO rule will force half of the dry bulk fleet into scrapping by 2027 because of carbon intensity ratings. Other posters disagree and say the current CII and EEXI rules only require slow steaming or efficiency upgrades. Nobody in the thread cites an official document.


Extract the factual claims that help answer the sub-question, following the rules you were given.

