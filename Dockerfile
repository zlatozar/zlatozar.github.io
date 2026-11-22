FROM ruby:3.2-alpine

RUN apk add --no-cache build-base \
 && gem install --no-document \
      jekyll:3.10.0 \
      kramdown-parser-gfm \
      jekyll-feed \
      jekyll-seo-tag \
      webrick

WORKDIR /srv/jekyll

EXPOSE 4000

CMD ["jekyll", "serve", "--host", "0.0.0.0", "--port", "4000"]
