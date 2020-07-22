#!/usr/bin/env bb
(ns hello-bb
  (:require
   [clojure.java.shell :as shell]
   [clojure.string :as string]))

(def date
  "Return current date in the format YYYY-MM-DD"
  (-> (shell/sh "date" "+%F")
      :out
      (string/trim)))

(let [[name] *command-line-args*]
  (println "hello" name "from bb script" date)
  (println (str "::set-output name=time::" date))
  (System/exit 0))