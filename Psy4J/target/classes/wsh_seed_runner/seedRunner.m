if startsWith(version, "9") == 0; error("[ERROR] ��ʹ�� MATLAB 9.0 �Լ����ߵİ汾���д˽ű�"); end

% ����õ����õ� Seed
ALL_TEST_PERIOD = 1000;
NEED_PRODUCT_NUMBER = 5;
DIFF_PRECISSION = 0.0000000001;

% �ȵõ�һ������ˮƽ�����ֵ

allAreaInfo = [];
for i = 1:ALL_TEST_PERIOD
    try
        [num, allNum] = getRandomPoint(5);
        [areaNow, ~] = computePointsArea(num);
        allAreaInfo = [allAreaInfo; areaNow];
    catch err
        disp("��WARN��������������飬���Ҽ��������ʷ��г��ִ���" + err.message)
    end
end

mostFreq = mode(allAreaInfo);
disp("�ظ� " + ALL_TEST_PERIOD + " �Σ����ճ��ֵ�Ƶ����������� " + mostFreq)

% Ȼ����ݴ����ֵ�����ɵ���

outAreaArray = [];
for i = 1:ALL_TEST_PERIOD
   try 
       [num, allNum] = getRandomPoint(5);
       [areaNow, ~] = computePointsArea(num);
       if abs(areaNow - mostFreq) < DIFF_PRECISSION
           outAreaArray = [outAreaArray; allNum];
           if length(outAreaArray) == 125
                break
           end
       end
   catch err
       disp("��WARN��������������飬���Ҽ��������ʷ��г��ִ���" + err.message)
   end
end

% ����˵��󵽶�� seed.log ��
tFArray = [];
for p = 1: length(outAreaArray)
    tF = outAreaArray(:,3);
    if tF(p) == 0
        tFArray = [tFArray; "false"];
    elseif tF(p) == 1
        tFArray = [tFArray; "true"];
    end
end

try
    fid = fopen("seed" + int32(rand(1,1)*1000) + ".log",'w');
    for t = 1: length(tFArray)
        fprintf(fid, '%s\n', tFArray(t));
    end
    fclose(fid);
catch err
    disp("�ڱ��浽 seed.log �ļ��г���" + err.message)
end
disp("�ѱ��浽 seed.log �ļ���") 
