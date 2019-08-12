function [array, allArray] = getRandomPoint(choosedPointNumber)
%GETRANDOMPONIT 根据传入的参数获取指定的点阵图，点阵图为 5 * 5 大小，
%返回值三列分别为 25 个点的 x、y、是否被选中（0，1）
temp = [];
for x = 0:4
    for y = 0:4
        temp = [temp;x,y];
    end
end

% 随机得到 5 个选中的点
noChoosed = repelem(0,25);
for i = 1:choosedPointNumber
    noChoosed(1,i) = 1;
end
choosedInfo = Shuffle(noChoosed);
noFilteredArray = [temp choosedInfo'];

filteredArray = [];
for j = 1:length(noFilteredArray)
   line = noFilteredArray(j,:);
   if line(3) == 1
       filteredArray = [filteredArray; line(1), line(2)];
   end
end

array = filteredArray;
allArray = noFilteredArray;
end

