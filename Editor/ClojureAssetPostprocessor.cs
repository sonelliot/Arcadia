using UnityEngine;
using UnityEditor;
using clojure.lang;
using System;

class ClojureAssetPostprocessor : AssetPostprocessor {
    static public void SetupLoadPath() {
        System.Environment.SetEnvironmentVariable("CLOJURE_LOAD_PATH", "Assets/Arcadia/Libraries");
        Debug.Log(System.Environment.GetEnvironmentVariable("CLOJURE_LOAD_PATH"));
        RT.load("arcadia/compiler");
        RT.var("arcadia.compiler", "setup-load-paths").invoke();
    }

    static public void OnPostprocessAllAssets(String[] importedAssets, String[] deletedAssets, String[] movedAssets, String[] movedFromAssetPaths) {
        RT.load("arcadia/compiler");
        RT.var("arcadia.compiler", "process-assets").invoke(importedAssets);        
    }
}