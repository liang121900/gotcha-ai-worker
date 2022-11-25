package com.gotcha.ai.worker.model

data class DetectionRequest(
    var modelName: String ="",
    var modelLocation: String ="",
    var cfgName: String ="",
    var cfgLocation: String ="",
    var fileName: String ="",
    var fileLocation: String ="",
    var generateDetectionImage: Boolean =false,
    var receiptHandle:String =""
)