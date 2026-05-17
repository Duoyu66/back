-- 用户界面偏好（主题色、暗色模式、圆角等）
USE admin;

ALTER TABLE sys_user
  ADD COLUMN preferences JSON NULL COMMENT '界面偏好 JSON' AFTER avatar;
