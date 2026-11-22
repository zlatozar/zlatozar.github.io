(function () {
  "use strict";

  // 1. Theme toggle
  var toggleBtn = document.getElementById("theme-toggle");
  if (toggleBtn) {
    toggleBtn.addEventListener("click", function () {
      var current = document.documentElement.getAttribute("data-theme") || "light";
      var next = current === "dark" ? "light" : "dark";
      document.documentElement.setAttribute("data-theme", next);
      try {
        localStorage.setItem("theme", next);
      } catch (e) {}
    });
  }

  // 2. Reading progress bar (posts)
  var progressBar = document.getElementById("reading-progress-bar");
  var postContent = document.getElementById("post-content");

  if (progressBar && postContent) {
    var updateProgress = function () {
      var rect = postContent.getBoundingClientRect();
      var total = postContent.offsetHeight - window.innerHeight * 0.55;
      var scrolled = -rect.top + window.innerHeight * 0.25;
      var pct = total > 0 ? Math.min(100, Math.max(0, (scrolled / total) * 100)) : 0;
      progressBar.style.width = pct.toFixed(1) + "%";
    };
    window.addEventListener("scroll", updateProgress, { passive: true });
    window.addEventListener("resize", updateProgress, { passive: true });
    updateProgress();
  }

  // 3. Wrap tables for clean horizontal scrolling
  var proseBlocks = document.querySelectorAll(".prose");
  proseBlocks.forEach(function (prose) {
    prose.querySelectorAll("table").forEach(function (table) {
      if (table.parentElement && table.parentElement.classList.contains("table-wrap")) return;
      var wrap = document.createElement("div");
      wrap.className = "table-wrap";
      table.parentNode.insertBefore(wrap, table);
      wrap.appendChild(table);
    });
  });

  // 4. Enhance code blocks with language badge & copy button
  document.querySelectorAll(".prose div.highlighter-rouge, .prose > pre").forEach(function (block) {
    var pre = block.tagName === "PRE" ? block : block.querySelector("pre");
    if (!pre || (block.parentElement && block.parentElement.classList.contains("code-block"))) return;

    var lang = "text";
    if (block.className) {
      var match = block.className.match(/language-([a-zA-Z0-9_-]+)/);
      if (match && match[1]) {
        lang = match[1] === "plaintext" ? "diagram / text" : match[1];
      }
    }

    var wrapper = document.createElement("div");
    wrapper.className = "code-block";

    var bar = document.createElement("div");
    bar.className = "code-block__bar";

    var langEl = document.createElement("span");
    langEl.className = "code-block__lang";
    langEl.textContent = lang;

    var copyBtn = document.createElement("button");
    copyBtn.type = "button";
    copyBtn.className = "code-block__copy";
    copyBtn.textContent = "Copy";

    copyBtn.addEventListener("click", function () {
      var codeText = pre.innerText;
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(codeText).then(function () {
          copyBtn.textContent = "Copied";
          setTimeout(function () {
            copyBtn.textContent = "Copy";
          }, 1600);
        });
      }
    });

    bar.appendChild(langEl);
    bar.appendChild(copyBtn);

    block.parentNode.insertBefore(wrapper, block);
    wrapper.appendChild(bar);
    wrapper.appendChild(block);
  });

  // 5. Build sticky Table of Contents & heading anchor links
  var tocAside = document.getElementById("post-toc");
  var tocNav = document.getElementById("post-toc-nav");
  var postLayout = document.querySelector(".post-layout");

  if (postContent) {
    var headings = Array.prototype.slice.call(postContent.querySelectorAll("h2, h3"));
    var tocEntries = [];
    headings.forEach(function (h) {
      var headingText = h.textContent.trim();
      if (!h.id) {
        h.id = headingText
          .toLowerCase()
          .replace(/[^\w\s-]/g, "")
          .replace(/\s+/g, "-");
      }
      tocEntries.push({ id: h.id, text: headingText, isSub: h.tagName === "H3", el: h });
      var anchor = document.createElement("a");
      anchor.className = "heading-anchor";
      anchor.href = "#" + h.id;
      anchor.setAttribute("aria-label", "Link to section: " + headingText);
      anchor.textContent = "#";
      h.appendChild(anchor);
    });

    if (tocAside && tocNav && tocEntries.length >= 2) {
      var list = document.createElement("ul");
      list.className = "post-toc__list";
      var linkMap = {};

      tocEntries.forEach(function (entry) {
        var li = document.createElement("li");
        var a = document.createElement("a");
        a.className = "post-toc__link" + (entry.isSub ? " is-sub" : "");
        a.href = "#" + entry.id;
        a.textContent = entry.text;
        li.appendChild(a);
        list.appendChild(li);
        linkMap[entry.id] = a;
      });

      tocNav.appendChild(list);
      tocAside.hidden = false;
      if (postLayout) postLayout.classList.add("has-toc");

      if ("IntersectionObserver" in window) {
        var observer = new IntersectionObserver(
          function (entries) {
            entries.forEach(function (entry) {
              if (entry.isIntersecting) {
                Object.keys(linkMap).forEach(function (id) {
                  linkMap[id].classList.remove("is-active");
                });
                if (linkMap[entry.target.id]) {
                  linkMap[entry.target.id].classList.add("is-active");
                }
              }
            });
          },
          { rootMargin: "-90px 0px -70% 0px", threshold: 0 }
        );
        headings.forEach(function (h) {
          observer.observe(h);
        });
      }
    }
  }
})();
