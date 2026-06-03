BEGIN
    EXECUTE IMMEDIATE '
        UPDATE "tb_product_categories"
        SET "updated_at" = "created_at"
        WHERE "updated_at" IS NULL';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE NOT IN (-942, -904) THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE '
        UPDATE "TB_PRODUCT_CATEGORIES"
        SET "UPDATED_AT" = "CREATED_AT"
        WHERE "UPDATED_AT" IS NULL';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE NOT IN (-942, -904) THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE '
        CREATE INDEX "idx_products_store_category_active"
        ON "tb_products" ("store_id", "category_id", "active")';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 AND SQLCODE != -942 AND SQLCODE != -904 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE '
        CREATE INDEX "IDX_PRODUCTS_STORE_CATEGORY_ACTIVE"
        ON "TB_PRODUCTS" ("STORE_ID", "CATEGORY_ID", "ACTIVE")';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 AND SQLCODE != -942 AND SQLCODE != -904 THEN
            RAISE;
        END IF;
END;
/
