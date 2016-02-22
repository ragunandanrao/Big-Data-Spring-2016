library(flexclust)
library(cluster)
library(fpc)
trainingData<-read.csv("/home/raghu/Training Data_Result prediction.csv")
trainingData[ncol(trainingData)]<-NULL
trainingData
trainedClusters<- kcca(trainingData,k=4,family=kccaFamily("kmedians"),save.data = TRUE)
trainedSet<-clusters(trainedClusters)
trainedSet
plot(trainedSet,col=trainedSet)
