def notifier = new me.digi.Slack(this);

node('osx') {
    try {
        stage('clean-workspace') {
            step([$class: 'WsCleanup'])
        }
        stage('checkout') {
            checkout(scm)
        }
        stage('lint') {
            //run static only on release build type
            sh "./gradlew lintRelease -PBUILD_NUMBER=${env.BUILD_NUMBER}"
        }
        stage('test') {
            sh "./gradlew test -PBUILD_NUMBER=${env.BUILD_NUMBER}"
        }
        stage('android-test') {
            sh "./launch_emulator.sh"
            sh "./gradlew connectedAndroidTest"
        }
        stage('build') {
            sh "./gradlew assembleRelease -PBUILD_NUMBER=${env.BUILD_NUMBER}"
        }
        stage('artifacts') {
            if (env.BRANCH_NAME == "master") {
                dir('digime-core/build/outputs/aar') {
                    archiveArtifacts artifacts: '*.aar', fingerprint: true;
                }
            }
        }
        stage('notify') {
            notifier.success()
        }
    } catch (err) {
        notifier.fail()
        currentBuild.result = "FAILURE"
        echo err.message
    }
}

