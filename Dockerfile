FROM clojure:temurin-8-tools-deps-bullseye
COPY . /app
WORKDIR /app
CMD clj -M:prod