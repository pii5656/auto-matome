(ns auto-matome.core
  (:gen-class)
  (:import (org.apache.lucene.analysis Analyzer TokenStream)
           (org.apache.lucene.analysis.ja JapaneseAnalyzer)
           (org.apache.lucene.analysis.ja.tokenattributes BaseFormAttribute InflectionAttribute PartOfSpeechAttribute ReadingAttribute)
           (org.apache.lucene.analysis.tokenattributes CharTermAttribute)
           (org.apache.lucene.util Version))
  (:use (incanter core stats charts io)
        [auto-matome.thread]
        [auto-matome.morpho])
  (:require [clojure.string :as str]))


(require '[auto-matome.scrape :as scr])
(require '[auto-matome.scrape-origin :as scro])
(require '[auto-matome.io :as io])

(def home-url "http://blog.livedoor.jp/dqnplus/")
(def page-num 1)
(def contents-resource "resource/contents.txt")
(def dictionary-path "resource/dictionary.txt")

(defn get-responses
  []
  (let [origin-urls (flatten (scr/select-hayabusa-thread-urls (scr/get-thread-urls home-url page-num)))]
    origin-urls
    (flatten
     (doall (map #(-> % scr/get-html-resource scro/get-responses) origin-urls)))
    )
  )

(defn make-words-set-from-text
  [file-path]
  (let [contents (io/read-contents file-path)
        analyzed (map #(-> % morphological-analysis-sentence) contents)
        words-set (set (flatten (map (fn [x] (map (fn [y] (first y)) x)) analyzed)))]
    words-set
  ))

(defn from-set-to-dictionary
  [words-set]
  (let [zipped (map-indexed #(vector %1 %2) words-set)]
    (loop [result {} tmp-zipped zipped]
      (let [zip (first tmp-zipped)]
        (if (empty? zip)
          result
          (recur (assoc result (second zip) (first zip)) (rest tmp-zipped)))))))

;  (let [size (count words-set)
;        range (range 1 size)
;        zipped (apply map list [words-set range])]
;;    (println zipped)
;    (loop [result {} zip (first zipped)]
;      (println zip)
;      (if (= zip nil)
;        result
;        (recur (assoc result (first zip) (second zip)) (rest zipped))
;        )
;      )
;    )
;  )

(defn -main
  [& args]
  (get-responses))

(defn test01
  []
  (let [origin-url "http://hayabusa3.2ch.sc/test/read.cgi/news/1505522180/"
        responses (-> origin-url scr/get-html-resource scro/get-responses)
        contents (map #(-> % :content) responses)]
    (io/write-strings contents contents-resource)
    )
  )

(defn test02
  []
;  (doseq [x (io/read-contents "resource/contents.txt")]
  (map #(-> % morphological-analysis-sentence) (io/read-contents "resource/contents.txt"))
  )

(defn test03
  []
  (let [responses (get-responses)
        contents (map #(-> % :content) responses)]
    (io/write-strings contents)
    ))

(defn test04
  []
  (make-words-set-from-text contents-resource))

(defn test05
  []
  (let [responses (get-responses)
        contents (map #(-> % :content) responses)
        buffer (io/write-strings contents contents-resource)
        words (make-words-set-from-text contents-resource)]
  (io/write-words words dictionary-path)
  ))

(defn test06
  []
  (let [;contents (io/read-contents contents-resource)
        words (make-words-set-from-text contents-resource)]
    (io/write-words words dictionary-path)
    ))

(defn test07
  []
  (let [words (make-words-set-from-text contents-resource)]
    (from-set-to-dictionary words)
  ))
;  (println "===== Simple Pattern =====")
;  (doseq [t (morphological-analysis-sentence "黒い大きな瞳の男の娘")]
;    (println t))
;
;  (println "===== Filter Pattern =====")
                                        ;  (doseq [t (morphological-analysis-sentence

;             "僕はウナギだし象は鼻が長い"
;             #(not (nil? (re-find #"名詞" (nth % 2)))))]
;    (println t)))

;;  (println "===== 坊ちゃん =====")
;;  (let [tokens (morphological-analysis-sentence (slurp "bocchan.txt")
;;                                                #(not (nil? (re-find #"名詞" (nth % 2)))))
;;        words (flatten (map #(first %) tokens))]
;;    (view (bar-chart (keys a(top10 words)) (vals (top10 words))))
;;    (save (bar-chart (keys (top10 words)) (vals (top10 words))) "natume.png" :width 600)
;;    (save (bar-chart (keys (top100 words)) (vals (top100 words))) "natume_zip.png" :width 600)))

