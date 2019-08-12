function [area, k] = computePointsArea(points)
%COMPUTEPOINTSAREA 计算点阵的外部点围成的面积（凸包）
% 先寻找外部点
x = points(:,1);
y = points(:,2);
dt = delaunayTriangulation(x,y);
% 计算这些点的凸包面积
[k, area] = convexHull(dt);
end

