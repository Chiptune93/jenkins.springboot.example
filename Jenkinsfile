pipeline {
    agent any

    environment {
        IMAGE_NAME = "spring-example"
        REMOTE_NAME = "PipelineRemoteServer"
        REMOTE_HOST =  "chiptune.iptime.org"
        REMOTE_CREDENTIAL_ID = "chiptune"
        SOURCE_FILES = "build/libs/example-0.0.1-SNAPSHOT.jar, dockerfile, docker-compose.yml"
        REMOTE_DIRECTORY = "/jenkins/jenkins_deploy/springboot_example"
    }

    stages {

        // 프로젝트 빌드 및 테스트
        stage("CI: Project Build") {
            steps {
                sh "./gradlew clean build test"
            }
        }

        // 배포 대상 서버에 dockerfile, docker-compose 파일, JAR 파일 전달
        stage("CI: Transfer Files") {
            steps {
                script {
                    // SSH 플러그인을 사용하여 파일 전송
                    sshPublisher(
                        publishers: [
                            sshPublisherDesc(
                                configName: "${REMOTE_NAME}",
                                transfers: [
                                    sshTransfer(
                                        execCommand: '', // 원격 명령 (비워둘 수 있음)
                                        execTimeout: 120000, // 명령 실행 제한 시간 (밀리초)
                                        flatten: false, // true로 설정하면 원격 경로에서 파일이 복사됩니다.
                                        makeEmptyDirs: false, // true로 설정하면 원격 디렉토리에 빈 디렉토리가 생성됩니다.
                                        noDefaultExcludes: false,
                                        patternSeparator: '[, ]+',
                                        remoteDirectory: "${REMOTE_DIRECTORY}",
                                        remoteDirectorySDF: false,
                                        removePrefix: '', // 원본 파일 경로에서 제거할 접두사
                                        sourceFiles: "${SOURCE_FILES}",
                                        verbose: true
                                    )
                                ]
                            )
                        ]
                    )
                }
            }
        }

        // 배포 서버에서 도커 이미지 빌드
        stage("CI: Docker Build") {
            steps {
                script {
                    def remoteServer = [
                        name: "${REMOTE_NAME}", // SSH 호스트 설정의 이름
                        host: "${REMOTE_HOST}", // 대상 서버 주소
                        credentialsId: "${REMOTE_CREDENTIAL_ID}" // Jenkins 자격 증명 ID (SSH 키 또는 사용자 이름/비밀번호)
                    ]

                    // 원격 서버에서 Docker 이미지 빌드 명령 실행
                    def nowPath = 'pwd & ls -al'
                    def nowPathResult = sshCommand remote: remoteServer, command: nowPath, returnStatus: true
                    if (nowPathResult == 0) {
                        echo "Command '${nowPath}' executed successfully."
                    } else {
                        echo "Command '${nowPath}' failed with exit code ${nowPathResult}."
                    }

                    def dockerBuild = 'docker build -t ${IMAGE_NAME}:latest .'
                    def dockerBuildResult = sshCommand remote: remoteServer, command: dockerBuild, returnStatus: true
                    if (dockerBuildResult == 0) {
                        echo "Command '${dockerBuild}' executed successfully."
                    } else {
                        echo "Command '${dockerBuild}' failed with exit code ${dockerBuildResult}."
                    }

                    // 빌드된 이미지를 Docker 레지스트리에 푸시 (옵션)
                    // sshCommand remote: remoteServer, command: 'docker push my-docker-image:latest'
                }
            }
        }

        // 배포 서버에서 도커 빌드 된 도커 이미지 실행
        stage("CD : Deploy") {
            steps {
                script {
                    def remoteServer = [
                        name: "${REMOTE_NAME}", // SSH 호스트 설정의 이름
                        host: "${REMOTE_HOST}", // 대상 서버 주소
                        credentialsId: "${REMOTE_CREDENTIAL_ID}" // Jenkins 자격 증명 ID (SSH 키 또는 사용자 이름/비밀번호)
                    ]

                    // Docker Compose를 사용하여 컨테이너 실행
                    def dockerComposeDeploy = 'docker compose -f /jenkins/jenkins_deploy/springboot_example/docker-compose.yml up -d'
                    def dockerComposeDeployResult = sshCommand remote: remoteServer, command: dockerComposeDeploy, returnStatus: true
                    if (dockerBuildResult == 0) {
                        echo "Command '${dockerComposeDeploy}' executed successfully."
                    } else {
                        echo "Command '${dockerComposeDeploy}' failed with exit code ${dockerComposeDeployResult}."
                    }
                }
            }
        }
    }
}
