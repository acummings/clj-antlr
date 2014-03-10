(ns clj-antlr.core-test
  (:import clj_antlr.ParseError)
  (:use clj-antlr.core
        clojure.test
        clojure.pprint))

(deftest interprets-json
  (let [json (parser "grammars/Json.g4")
        sexpr (json "{\"nums\": [1,2,3]}")]
    (is (= sexpr
           '(:jsonText
              (:jsonObject
                "{"
                (:member
                  "\"nums\""
                  ":"
                  (:jsonValue
                    (:jsonArray
                      "["
                      (:jsonValue (:jsonNumber "1"))
                      ","
                      (:jsonValue (:jsonNumber "2"))
                      ","
                      (:jsonValue (:jsonNumber "3"))
                      "]")))
                "}"))))))

(deftest error-test
  (let [json (parser "grammars/Json.g4")]
    (testing "throws on tokenization errors"
      (let [^ParseError err (try (parse json "[1, \uf000, 2]")
                                 (catch ParseError e e))]
        (is (= (.getMessage err) "token recognition error at: ''\nextraneous input ',' expecting {'null', '{', '[', 'false', 'true', NUMBER, STRING}"))))

    (testing "throws on recognition errors"
      (let [^ParseError err (try (parse json "[1,2,,5,]")
                                 (catch ParseError e e))]
        (is (= (.getMessage err)
               "extraneous input ',' expecting {'null', '{', '[', 'false', 'true', NUMBER, STRING}
mismatched input ']' expecting {'null', '{', '[', 'false', 'true', NUMBER, STRING}"))
        (is (= (map :line @err) [1 1]))
        (is (= (map :char @err) [5 8]))))))
