function [array, allArray] = getRandomPoint(choosedPointNumber)
%GETRANDOMPONIT ���ݴ���Ĳ�����ȡָ���ĵ���ͼ������ͼΪ 5 * 5 ��С��
%����ֵ���зֱ�Ϊ 25 ����� x��y���Ƿ�ѡ�У�0��1��
temp = [];
for x = 0:4
    for y = 0:4
        temp = [temp;x,y];
    end
end

% ����õ� 5 ��ѡ�еĵ�
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

