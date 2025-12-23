pipeline {
    agent {
        docker {
            image 'maven:3.9.11-eclipse-temurin-17'
            args '-v $HOME/.m2:/root/.m2'  // cache Maven
        }
    }

    environment {
        REGISTRY = "mzakymaizi"    // Username DockerHub
        IMAGE_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                sh """
                    mvn -f anggota/pom.xml clean package -DskipTests
                    mvn -f buku/pom.xml clean package -DskipTests
                    mvn -f peminjaman/pom.xml clean package -DskipTests
                    mvn -f pengembalian_service/pom.xml clean package -DskipTests
                    mvn -f api_gateway/pom.xml clean package -DskipTests
                    mvn -f eureka-server/pom.xml clean package -DskipTests
                """
            }
        }

        stage('Build Docker Images') {
            parallel {
                stage('anggota-service') {
                    steps {
                        script {
                            docker.build("${REGISTRY}/anggota-service:${IMAGE_TAG}", "./anggota")
                        }
                    }
                }
                stage('buku-service') {
                    steps {
                        script {
                            docker.build("${REGISTRY}/buku-service:${IMAGE_TAG}", "./buku")
                        }
                    }
                }
                stage('peminjaman-service') {
                    steps {
                        script {
                            docker.build("${REGISTRY}/peminjaman-service:${IMAGE_TAG}", "./peminjaman")
                        }
                    }
                }
                stage('pengembalian-service') {
                    steps {
                        script {
                            docker.build("${REGISTRY}/pengembalian-service:${IMAGE_TAG}", "./pengembalian_service")
                        }
                    }
                }
                stage('api-gateway') {
                    steps {
                        script {
                            docker.build("${REGISTRY}/api-gateway:${IMAGE_TAG}", "./api_gateway")
                        }
                    }
                }
                stage('eureka-server') {
                    steps {
                        script {
                            docker.build("${REGISTRY}/eureka-server:${IMAGE_TAG}", "./eureka-server")
                        }
                    }
                }
                stage('logstash') {
                    steps {
                        script {
                            docker.build("${REGISTRY}/logstash:${IMAGE_TAG}", "./logstash")
                        }
                    }
                }
            }
        }

        stage('Login DockerHub') {
            steps {
                withCredentials([string(credentialsId: 'dockerhub-token', variable: 'DOCKER_TOKEN')]) {
                    sh """
                        echo "$DOCKER_TOKEN" | docker login -u "${REGISTRY}" --password-stdin
                    """
                }
            }
        }

        stage('Push Images') {
            parallel {
                stage('push-anggota') { steps { sh "docker push ${REGISTRY}/anggota-service:${IMAGE_TAG}" } }
                stage('push-buku') { steps { sh "docker push ${REGISTRY}/buku-service:${IMAGE_TAG}" } }
                stage('push-peminjaman') { steps { sh "docker push ${REGISTRY}/peminjaman-service:${IMAGE_TAG}" } }
                stage('push-pengembalian') { steps { sh "docker push ${REGISTRY}/pengembalian-service:${IMAGE_TAG}" } }
                stage('push-api-gateway') { steps { sh "docker push ${REGISTRY}/api-gateway:${IMAGE_TAG}" } }
                stage('push-eureka') { steps { sh "docker push ${REGISTRY}/eureka-server:${IMAGE_TAG}" } }
                stage('push-logstash') { steps { sh "docker push ${REGISTRY}/logstash:${IMAGE_TAG}" } }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh "kubectl apply -f manifests/"
            }
        }
    }

    post {
        success {
            echo "Deployment berhasil!"
        }
        failure {
            echo "Pipeline gagal."
        }
    }
}
