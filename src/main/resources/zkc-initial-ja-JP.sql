delete from h_menus where app='h-zkc' and name in ('Dashboard','Main','Query','History','Backup','Refresh','Encrypt');
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-zkc','Dashboard','コンソール','/Dashboard','-',1,1,'DashboardIcon',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-zkc','Main','構成管理','inner:${menuPrefixPath}/h-zkc/hbq969-zkc/index.html#/main','Dashboard',0,2,'conffile',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-zkc','Query','クエリーの設定','inner:${menuPrefixPath}/h-zkc/hbq969-zkc/index.html#/query','Dashboard',1,2,'handbook',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-zkc','History','履歴操作','inner:${menuPrefixPath}/h-zkc/hbq969-zkc/index.html#/history','Dashboard',2,2,'rule',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-zkc','Backup','バックアップ・リカバリ','inner:${menuPrefixPath}/h-zkc/hbq969-zkc/index.html#/back','Dashboard',3,2,'backup1',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-zkc','Encrypt','暗号化ツール','inner:${menuPrefixPath}/h-zkc/hbq969-common/index.html#/encrypt','Dashboard',4,2,'encrypt1',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-zkc','Refresh','構成の更新','inner:${menuPrefixPath}/h-zkc/hbq969-zkc/index.html#/refresh','Dashboard',5,2,'sync',1735800456);

delete from h_sm_info where app='h-zkc';
insert into h_sm_info(app,info_content) values('h-zkc','{"title":"Zookeeper コンソール"}');