if startsWith(version, "9") == 0; error("[ERROR] 请使用 MATLAB 9.0 以及更高的版本运行此脚本"); end

% 计算得到所用的 Seed
ALL_TEST_PERIOD = 1000;
NEED_PRODUCT_NUMBER = 5;
DIFF_PRECISSION = 0.0000000001;

% 先得到一个合适水平的面积值

allAreaInfo = [];
for i = 1:ALL_TEST_PERIOD
    try
        [num, allNum] = getRandomPoint(5);
        [areaNow, ~] = computePointsArea(num);
        allAreaInfo = [allAreaInfo; areaNow];
    catch err
        disp("【WARN】在随机生成数组，并且计算三角剖分中出现错误：" + err.message)
    end
end

mostFreq = mode(allAreaInfo);
disp("重复 " + ALL_TEST_PERIOD + " 次，最终出现的频率最大的面积是 " + mostFreq)

% 然后根据此面积值来生成点阵

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
       disp("【WARN】在随机计算数组，并且计算三角剖分中出现错误：" + err.message)
   end
end

% 输出此点阵到多个 seed.log 中
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
    disp("在保存到 seed.log 文件中出错：" + err.message)
end
disp("已保存到 seed.log 文件中") 
