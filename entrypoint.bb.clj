#!/usr/bin/env bb
;;; GitHub Docker action entrypoint: parse the action params and excecute the
;;; babashka script or commands.
;;; https://github.com/borkdude/babashka
(ns entrypoint
  (:require
   [clojure.java.shell :as shell]
   [clojure.string :as string]
   [clojure.java.io :as io]
   [clojure.tools.cli :as cli]))

(def exit-codes
  {:success      0
   :args-error   4
   :general-err  8})

(def args-usage
  (str "\n"
       "usage: <bb_src>  path to babasha script to execute\n"
       "       <bb_args> (optional) babashka script arguments\n"
       "usage: <bb_url>  URL of the remote babasha script to download and execute\n"
       "       <bb_args> (optional) babashka script arguments\n"
       "usage: <bb_cmd>  shell command(s) piped with babashka command(s) to execute"))

(def cli-options
  [["" "--bb_src <path-to-babashka-script>" ""
    :default ""
    :validate [#(if (seq %) (.exists (io/file %)) true) "babashka script not found"]]
   ["" "--bb_url \"URL of the remote babashka script to download and execute\"" ""
    :default ""]
   ["" "--bb_cmd <shell command(s) piped with babashak command(s)>" ""
    :default ""]
   ["" "--bb_args \"babashka script arguments\"" ""
    :default ""]
   ["-h" "--help"
    :default false]])

(defn args-error [errors]
  (str "\nthe following errors occurred while parsing your command:\n"
       (string/join \newline errors)))

;;; https://docs.github.com/en/actions/creating-actions/creating-a-docker-container-action#writing-the-action-code
(defn exit-message
  "Return a formatted output message for the Docker action workflow"
  [message]
  (str "::set-output name=bb_out::" message))

(defn parse-args
  "Parse and validate command line arguments. Either return a map indicating
  the program should exit (with a error message and status code), or a map of
  options provided"
  [args]
  (let [{:keys [options errors]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) {:message args-usage :exit-code :success}
      errors          {:message (args-error errors) :exit-code :args-error}
      (or (and (not (string/blank? (:bb_src options)))
               (not (string/blank? (:bb_cmd options))))
          (and (not (string/blank? (:bb_src options)))
               (not (string/blank? (:bb_url options))))
          (and (not (string/blank? (:bb_url options)))
               (not (string/blank? (:bb_cmd options)))))
      {:message args-usage :exit-code :args-error}
      :else           {:opts options})))

(defn system-exit!
  [message exit-code]
  (let [code (get exit-codes exit-code exit-code)]
    (println (exit-message message))
    (System/exit code)))

(defn download-script
  [script-url]
  (let [tmp-file (str (.toString (java.util.UUID/randomUUID)) ".clj")
        response (shell/sh "curl" "-s"
                           "-o" tmp-file
                           "-w" "\"%{http_code}\""
                           script-url)]
    (assoc response :script tmp-file)))

(defn exec-remote-script!
  "download nad exceute remote babashka script with specified arguments"
  [url args]
  (let [{:keys [exit out script]} (download-script url)
        status (Integer/parseInt (string/replace out #"\"" ""))]
    (if (and (zero? exit) (= status 200))
      (let [{:keys [exit err out]} (shell/sh "bb" "-f" script args)]
        (shell/sh "rm" script)
        (if (zero? exit)
          (system-exit! out :success)
          (let [msg (if (string/blank? err)
                      (format "failed to execute remote script [%s]: exit code [%d]" url exit)
                      err)]
            (println msg)
            (system-exit! msg exit))))
      (let [msg (format "failed to download script [%s]: http status [%s] exit code [%d]" url out status)]
        (shell/sh "rm" script)
        (println msg)
        (system-exit! msg status)))))

(defn exec-script!
  "exceute babashka script with specified arguments"
  [src args]
  (let [{:keys [exit err out]} (shell/sh "bb" "-f" src args)]
    (if (zero? exit)
      (system-exit! out :success)
      (let [msg (if (string/blank? err)
                  (format "failed to execute script [%s]: exit code [%d]" src exit)
                  err)]
        (println msg)
        (system-exit! msg exit)))))

(defn exec-command!
  "execute babashka shell command(s)"
  [cmd]
  (let [{:keys [exit err out]} (shell/sh "sh" "-c" cmd)]
    (if (zero? exit)
      (system-exit! out :success)
      (let [msg (if (string/blank? err)
                  (format "failed to execute command [%s]: exit code [%d]" cmd exit)
                  err)]
        (println msg)
        (system-exit! msg exit)))))

;; script entry point
(let [args (parse-args *command-line-args*)]
  (if (:exit-code args)
    (system-exit! (:message args) (:exit-code args))
    (let [opts (:opts args)]
      (cond
        (seq (:bb_src opts)) (exec-script! (:bb_src opts) (:bb_args opts))
        (seq (:bb_url opts)) (exec-remote-script! (:bb_url opts) (:bb_args opts))
        (seq (:bb_cmd opts)) (exec-command! (:bb_cmd opts))
        :else                (system-exit! "wrong input params" :general-err)))))