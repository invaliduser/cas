#!/usr/local/bin/bb
(require '[babashka.process :refer [sh shell]])

(def *IMAGE-NAME* "cas-app:v1")
(def *CONTAINER-NAME* "cas-app")

(defn docker-build []
  (shell "docker"  "build" "-t" *IMAGE-NAME* ".")
)

(defn shadow-release []
  (shell "shadow-cljs release :app"))

(defn build []
  (shadow-release)
  (docker-build))

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
   "debug" docker-debug
   "run" docker-run
   "shadow-release" shadow-release})

((get commands (first *command-line-args*) default))
