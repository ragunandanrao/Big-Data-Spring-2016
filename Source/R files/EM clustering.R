library(flexclust)
library(cluster)
library(fpc)
library(mclust)
trainingData<-read.csv("/home/raghu/Training Data_Result prediction.csv")
resultColumn<-trainingData[ncol(trainingData)]
trainingData[ncol(trainingData)]<-NULL
mc <- Mclust(trainingData)
mc$classification
plot(mc$classification)