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
                    // SSH 플러그인을 사용하여 명령 실행
                    sshPublisher(
                        publishers: [
                            sshPublisherDesc(
                                configName: "${REMOTE_NAME}",
                                verbose: true,
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
                                        sourceFiles: "${SOURCE_FILES}"
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
                    // SSH 플러그인을 사용하여 파일 전송
                    sshPublisher(
                        publishers: [
                            sshPublisherDesc(
                                configName: "${REMOTE_NAME}",
                                verbose: true,
                                transfers: [
                                    sshTransfer(
                                        execCommand: """cd ~${REMOTE_DIRECTORY}"""
                                    ),
                                    sshTransfer(
                                        flatten: false, // true로 설정하면 원격 경로에서 파일이 복사됩니다.
                                        makeEmptyDirs: false, // true로 설정하면 원격 디렉토리에 빈 디렉토리가 생성됩니다.
                                        noDefaultExcludes: false,
                                        patternSeparator: '[, ]+',
                                        remoteDirectory: '',
                                        remoteDirectorySDF: false,
                                        execCommand: """pwd & docker build -t ${IMAGE_NAME} .""" // 원격 명령 (비워둘 수 있음)
                                    )
                                ]
                            )
                        ]
                    )
                }
            }
        }

        // 배포 서버에서 도커 빌드 된 도커 이미지 실행
        stage("CD : Deploy") {
            steps {
                script {
                    // SSH 플러그인을 사용하여 파일 전송
                    sshPublisher(
                        publishers: [
                            sshPublisherDesc(
                                configName: "${REMOTE_NAME}",
                                verbose: true,
                                transfers: [
                                    sshTransfer(
                                        execCommand: """cd ~${REMOTE_DIRECTORY}"""
                                    ),
                                    sshTransfer(
                                        flatten: false, // true로 설정하면 원격 경로에서 파일이 복사됩니다.
                                        makeEmptyDirs: false, // true로 설정하면 원격 디렉토리에 빈 디렉토리가 생성됩니다.
                                        noDefaultExcludes: false,
                                        patternSeparator: '[, ]+',
                                        remoteDirectory: '',
                                        remoteDirectorySDF: false,
                                        execCommand: 'pwd & docker compose up -d' // 원격 명령 (비워둘 수 있음)
                                    )
                                ]
                            )
                        ]
                    )
                }
            }
        }
    }
}
