---
layout: page
title: Archive
eyebrow: Complete Index
subtitle: All essays and technical notes published here, grouped by year and topic.
permalink: /archive/
---

{%- if site.tags.size > 0 -%}
<section class="archive-topics" aria-label="Topics">
  <h2 class="archive-year__heading">Topics</h2>
  <ul class="tag-row">
    {%- assign sorted_tags = site.tags | sort -%}
    {%- for tag in sorted_tags -%}
      <li class="tag">{{ tag[0] }} <span class="tag__count">({{ tag[1].size }})</span></li>
    {%- endfor -%}
  </ul>
</section>
{%- endif -%}

{%- assign posts_by_year = site.posts | group_by_exp: "post", "post.date | date: '%Y'" -%}
{%- for year in posts_by_year -%}
<section class="archive-year">
  <h2 class="archive-year__heading">{{ year.name }}</h2>
  <ul class="archive-list">
    {%- for post in year.items -%}
      <li class="archive-item">
        <time class="archive-item__date" datetime="{{ post.date | date_to_xmlschema }}">
          {{ post.date | date: "%b %-d, %Y" }}
        </time>
        <a class="archive-item__title" href="{{ post.url | relative_url }}">
          {{ post.title | escape }}
        </a>
        {%- if post.categories.size > 0 -%}
          <span class="archive-item__cat">{{ post.categories | first }}</span>
        {%- endif -%}
      </li>
    {%- endfor -%}
  </ul>
</section>
{%- endfor -%}
