-- INSERT INTO users (USER_ID, CREATED_AT, UPDATED_AT, BIRTH, CELL_PHONE, FLAG, NAME, NICKNAME, POINT, PROFILE_IMAGE, SEX) VALUES (1, null, null, '2025-03-22','01011112222',false,'momo','mo',5000,null,'MALE');
-- INSERT INTO category (CATEGORY_ID, CREATED_AT, UPDATED_AT, CATEGORY_NAME, LEVEL, PARENT_ID) VALUES (1, null, null, '남성', 0, null);
-- INSERT INTO product (PRODUCT_ID,BRAND_NAME,DESCRIPTION,PRICE,PRODUCT_NAME,CATEGORY_ID,USER_ID,CREATED_AT,UPDATED_AT, PURCHASE_COUNT, VIEW_COUNT) VALUES (1,'베리시','짱짱',10000,'무지반팔',1,1,null,null,0,0);
-- INSERT INTO product_item (ITEM_ID, CREATED_AT, UPDATED_AT, COLOR, COLOR_CODE, QUANTITY, SIZE, PRODUCT_ID) VALUES (1, null, null, 'pink','#111111', 50, 'XL', 1);
-- INSERT INTO orders (ORDER_ID, CANCEL_STATUS, EXPIRATION_TIME, ORDERED_AT, PAYMENT_AMOUNT, PAYMENT_ID, PAYMENT_TYPE, POINT_SAVE, POINT_USAGE, STATUS, TOSS_ORDER_ID, TOTAL_AMOUNT, DELIVERY_ID, USER_ID) VALUES (1,'NONE',DATEADD(MINUTE, 30, NOW()),NOW(),null,'PAY-20250213132349-6572',null,0,0,'PENDING','ORD-20250213132349-6572',50000,null,1);
-- INSERT INTO order_details (ORDER_DETAIL_ID, BRAND_NAME, CART_ID, PRICE, PRODUCT_NAME, QUANTITY, SIZE, ORDER_ID, ITEM_ID) VALUES (1, '베리시',null,10000,'무지반팔',5,'XL',1,1);
INSERT INTO users (user_id, created_at, updated_at, birth, cell_phone, flag, name, nickname, point, profile_image, sex)
VALUES (1, null, null, '2025-03-22', '01011112222', false, 'momo', 'mo', 5000, null, 'male');

INSERT INTO category (category_id, created_at, updated_at, category_name, level, parent_id)
VALUES (1, null, null, '남성', 0, null);

INSERT INTO product (product_id, brand_name, description, price, product_name, category_id, user_id, created_at, updated_at, purchase_count, view_count)
VALUES (1, '베리시', '짱짱', 10000, '무지반팔', 1, 1, null, null, 0, 0);

INSERT INTO product_item (item_id, created_at, updated_at, color, color_code, quantity, size, product_id)
VALUES (1, null, null, 'pink', '#111111', 50, 'XL', 1);

INSERT INTO orders (order_id, cancel_status, expiration_time, ordered_at, payment_amount, payment_id, payment_type, point_save, point_usage, status, toss_order_id, total_amount, delivery_id, user_id)
VALUES (1, 'none', DATE_ADD(NOW(), INTERVAL 30 MINUTE), NOW(), null, 'pay-20250213132349-6572', null, 0, 0, 'pending', 'ord-20250213132349-6572', 50000, null, 1);

INSERT INTO order_details (order_detail_id, brand_name, cart_id, price, product_name, quantity, size, order_id, item_id)
VALUES (1, '베리시', 1, 10000, '무지반팔', 5, 'XL', 1, 1);

INSERT INTO cart (cart_id, created_at, updated_at, quantity, item_id, user_id)
VALUES (1,null,null,5,1,1);

-- INSERT INTO delivery (addr_detail, number_address, receiver_name, receiver_phone, road_address, status, order_id)
-- VALUES ("조심히 해주세요", "12345","momo","010-5134-1111","판교판교","READY",1);