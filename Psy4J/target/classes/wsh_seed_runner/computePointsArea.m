function [area, k] = computePointsArea(points)
%COMPUTEPOINTSAREA ���������ⲿ��Χ�ɵ������͹����
% ��Ѱ���ⲿ��
x = points(:,1);
y = points(:,2);
dt = delaunayTriangulation(x,y);
% ������Щ���͹�����
[k, area] = convexHull(dt);
end

