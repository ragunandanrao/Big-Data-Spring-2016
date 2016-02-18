library(flexclust)
library(cluster)
library(fpc)
trainingData<-read.csv("/home/raghu/Training Data_Result prediction.csv")
resultColumn<-trainingData[ncol(trainingData)]
trainingData[ncol(trainingData)]<-NULL
distance <- dist(trainingData, method="euclidean")
cluster <- hclust(distance, method="average")
plot(cluster, hang=-1, label=trainingData$Result)
