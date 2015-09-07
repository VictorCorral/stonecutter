(ns stonecutter.test.view.change-password
  (:require [midje.sweet :refer :all]
            [net.cgrand.enlive-html :as html]
            [stonecutter.test.view.test-helpers :as th]
            [stonecutter.routes :as r]
            [stonecutter.view.change-password :refer [change-password-form
                                                      current-password-error-translation-key]]))

(fact "should return some html"
      (let [page (-> (th/create-request)
                     change-password-form)]
        (html/select page [:body]) =not=> empty?))

(fact "work in progress should be removed from page"
      (let [page (-> (th/create-request) change-password-form)]
        page => th/work-in-progress-removed))

(fact (th/test-translations "Change password" change-password-form))

(fact "form posts to correct endpoint"
      (let [page (-> (th/create-request) change-password-form)]
        page => (th/has-form-action? (r/path :change-password))))

(fact "cancel link should go to correct endpoint"
      (let [page (-> (th/create-request) change-password-form)]
        (-> page (html/select [:.clj--change-password-cancel__link]) first :attrs :href) => (r/path :show-profile)))

(facts "about removing elements when there are no errors"
       (let [page (-> (th/create-request) change-password-form)]
         (fact "no elements have class for styling errors"
               (html/select page [:.form-row--validation-error]) => empty?)
         (fact "validation summary element is not shown by styling class"
               (-> page
                   (html/select [:.clj--validation-summary])
                   first
                   :attrs
                   :class) =not=> (contains "validation-summary--show"))))

(fact "page has script link to javascript file"
      (let [page (-> (th/create-request) change-password-form)]
        (html/select page [[:script (html/attr-has :src "js/change_password.js")]]) =not=> empty?))

(facts "about displaying errors"
       (tabular
         (let [page (-> (th/create-request {} ?errors) change-password-form)]
           (fact "validation-summary--show class is added to the validation summary element"
                 (-> (html/select page [[:.clj--validation-summary :.validation-summary--show]])
                     ) =not=> empty?)
           (fact "validation message is present as a validation summary item"
                 (html/select page [:.clj--validation-summary__item]) =not=> empty?)
           (fact "correct error messages are displayed"
                 (->> (html/select page [:.clj--validation-summary__item])
                      (map #(get-in % [:attrs :data-l8n]))) => ?validation-translations)
           (fact "correct elements are highlighted"
                 (->> (html/select page [:.form-row--validation-error])
                      (map #(get-in % [:attrs :class]))) => (contains ?highlighted-elements)))

         ?errors                          ?validation-translations                                                          ?highlighted-elements
         {:current-password :blank}       [current-password-error-translation-key]                                          [#"clj--current-password"]
         {:current-password :too-short}   [current-password-error-translation-key]                                          [#"clj--current-password"]
         {:current-password :too-long}    [current-password-error-translation-key]                                          [#"clj--current-password"]
         {:current-password :invalid}     [current-password-error-translation-key]                                          [#"clj--current-password"]
         {:new-password :blank}           ["content:change-password-form/new-password-blank-validation-message"]            [#"clj--new-password"]
         {:new-password :too-short}       ["content:change-password-form/new-password-too-short-validation-message"]        [#"clj--new-password"]
         {:new-password :too-long}        ["content:change-password-form/new-password-too-long-validation-message"]         [#"clj--new-password"]
         {:new-password :unchanged}       ["content:change-password-form/new-password-unchanged-validation-message"]        [#"clj--new-password"]
         {:current-password :blank
          :new-password :too-short}       [current-password-error-translation-key
                                           "content:change-password-form/new-password-too-short-validation-message"]        [#"clj--current-password"
                                                                                                                             #"clj--new-password"]))
