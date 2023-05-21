#!/usr/local/bin/bb
(require '[babashka.process :refer [sh shell]])

(def *CONTAINER-NAME* "cas-app")
(def *IMAGE-NAME* "cas-app")
(def *TAG* "latest")
(def *TAGGED-IMAGE* (str *IMAGE-NAME* ":" *TAG*))
(def *REGISTRY* "registry.digitalocean.com/daniel-reg")
(def *QUALIFIED-IMAGE* (str *REGISTRY* "/" *TAGGED-IMAGE*))

(defn docker-build []
  (shell "docker"  "build" "-t" *IMAGE-NAME* "."))

(defn shadow-release []
  (shell "shadow-cljs release :app"))

(defn set-update []
  (shell "scp /tmp/update.sh cas-app:/root/update.sh"))

(defn build []
  (shadow-release)
  (docker-build))

(defn do-push []
  (shell "docker tag" *TAGGED-IMAGE* *QUALIFIED-IMAGE*)
  (shell "docker push" *QUALIFIED-IMAGE*))

(defn docker-run []
  (shell "docker create --rm"
         "--name" *CONTAINER-NAME*
         "-p" "8080:8080"
         "-p" "50504:50504"
         "-p" "50505:50505"
         *IMAGE-NAME*)
  (shell "docker" "start" *CONTAINER-NAME*))

(defn docker-debug []
  (shell "docker run -it --rm --name" *CONTAINER-NAME*
         "-p" "8080:8080"
         "-p" "50504:50504"
         "-p" "50505:50505"
         *IMAGE-NAME*
         "/bin/bash"))

(declare commands)
(defn default []
  (println "need one of:" (->> commands keys (interpose ", ") (apply str) )))

(def commands
  {"build" build
   "docker-build" docker-build
   "do-push" do-push
   "debug" docker-debug
   "run" docker-run
   "shadow-release" shadow-release
   "set-update" set-update
   })

((get commands (first *command-line-args*) default))
