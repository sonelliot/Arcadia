(ns mine
  (:use unity.core)
  (:require [unity.hydrate :as h])
  (:import [UnityEngine
            Resources Debug RequireComponent
            Rigidbody GameObject Component
            Vector3]))

;; ============================================================
;; stuff we should put in core
;; ============================================================

(defn v3  ^Vector3 [x y z]
  (Vector3. x y z))

(defn destroy [x]
  (cond
    (instance? GameObject x)
    (let [^GameObject obj x]
      (if UnityEngine.Application/isPlaying
        (UnityEngine.Object/Destroy obj)
        (UnityEngine.Object/DestroyImmediate obj)))
    
    (instance? Component x)
    (let [^Component cmp x]
      (if UnityEngine.Application/isPlaying
        (UnityEngine.Object/Destroy cmp)
        (UnityEngine.Object/DestroyImmediate cmp)))

    :else
    (throw
      (System.ArgumentException.
        (str "Expecting GameObject or Component, instead getting " (type x))))))

(defmacro with-temporary-object [[name init] & body]
  `(let [~name ~init
         retval# (do ~@body)]
     (destroy ~name)
     retval#))

(defn cube []
  (GameObject/CreatePrimitive PrimitiveType/Cube))

(defn nab-mesh [^GameObject obj]
  (.. obj (GetComponent UnityEngine.MeshFilter) sharedMesh))

(defn nab-material [^GameObject obj]
  (.. obj (GetComponent UnityEngine.MeshRenderer) sharedMaterial))

(defn all-objects []
  (GameObject/FindObjectsOfType (type-args UnityEngine.GameObject)))

(defn kill-by-name [name]
  (doseq [^GameObject obj (all-objects)
          :when (= (.name obj) name)]
    (UnityEngine.Object/DestroyImmediate obj)))

; ;; ============================================================
; ;; basic resources
; ;; ============================================================

(def cube-mesh
  (with-temporary-object [c (cube)]
    (nab-mesh c)))

(def default-material
  (with-temporary-object [c (cube)]
    (nab-material c)))

; ;; ============================================================
; ;; minelands
; ;; ============================================================

(defcomponent ^{RequireComponent Rigidbody} VoxelTexture
  [^System.String texure]
  (Start [this]
         (Debug/Log (str "Textures/" texure))
         (set! (.. this renderer material mainTexture) 
               (Resources/Load (str "Textures/" texure)))))

(defn one-of [& branches]
  (rand-nth branches))

(defmacro angry-lte [& xs]
  (cons 'and (map #(cons '<= %) (partition 2 1 xs))))

(defn texture-at-height
  ([^double height ^double max-height] (texture-at-height (/ height max-height)))
  ([^double height]
     (cond
       (angry-lte height 0.25)
       (one-of
         "bedrock")
       (angry-lte 0.25 height 0.3)
       (one-of
         "gravel")
       (angry-lte 0.3 height 0.5)
       (one-of
         "grass_top")
       (angry-lte 0.5 height 0.6)
       (one-of
         "cobblestone"
         "cobblestone_mossy")
       (angry-lte 0.6 height 0.75)
       (one-of
         "snow")
       (angry-lte 0.75 height)
       (one-of
         "ice"))))

(defn test-transform-check-1 [x]
  ((fn [x']
     (= UnityEngine.Transform (type x')))
   x))

(let [trnst UnityEngine.Transform]
  (defn test-transform-check-2 [x]
    ((fn [x']
       (= trnst (type x')))
     x)))

(defmacro noise
    ([x] `(Mathf/PerlinNoise (+ 0.1 ~x) 0))
    ([x y] `(Mathf/PerlinNoise (+ 0.1 ~x) ~y)))

(defn make-land []
  (let [vx (GameObject/Find "Voxel"), dim 10]
    (doseq [x (range dim)
            z (range dim)]
      (let [height (int (+ 1 (* 20 (noise 
                                     (* 0.05 x)
                                     (* 0.05 z)))))]
        (doseq [^double y (range height)]
          (if (= y (- height 1)) 
            (let [tex (Resources/Load (str "Textures/" (texture-at-height y 20)))
                  ^GameObject obj (GameObject/Instantiate vx)
                  ren (.GetComponent obj UnityEngine.Renderer)]
              (h/populate-game-object! obj
                {:name "Voxel"
                 :transform [{:position (v3 x y z)}]
                 :box-collider [{:extents (v3 0.5 0.5 0.5)}]
                 :rigidbody [{:mass 10
                              :is-kinematic true}]})
              (set! (.. ren material mainTexture) tex))))))))

(defcomponent land-maker []
  (Awake [_]
    (require 'mine))
  
  (Update [this]
    (when Input/anyKeyDown
      ;(test-transform-check-1 :bla)
      ;(test-transform-check-2 :bla)
      (make-land)
      (Debug/Log "hitting update like a pro")
      ;(make-land)
      )))

