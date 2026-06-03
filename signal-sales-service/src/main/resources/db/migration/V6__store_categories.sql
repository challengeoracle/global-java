BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE "tb_product_categories" ADD "store_id" RAW(16)';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -1430 AND SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE "TB_PRODUCT_CATEGORIES" ADD "STORE_ID" RAW(16)';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -1430 AND SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE "tb_product_categories" ADD "updated_at" TIMESTAMP';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -1430 AND SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE "TB_PRODUCT_CATEGORIES" ADD "UPDATED_AT" TIMESTAMP';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -1430 AND SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

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
        UPDATE "tb_product_categories" c
        SET c."store_id" = (
            SELECT MIN(p."store_id")
            FROM "tb_products" p
            WHERE p."category_id" = c."id"
        )
        WHERE c."store_id" IS NULL
          AND EXISTS (
            SELECT 1
            FROM "tb_products" p
            WHERE p."category_id" = c."id"
          )';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE NOT IN (-942, -904) THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE '
        UPDATE "TB_PRODUCT_CATEGORIES" c
        SET c."STORE_ID" = (
            SELECT MIN(p."STORE_ID")
            FROM "TB_PRODUCTS" p
            WHERE p."CATEGORY_ID" = c."ID"
        )
        WHERE c."STORE_ID" IS NULL
          AND EXISTS (
            SELECT 1
            FROM "TB_PRODUCTS" p
            WHERE p."CATEGORY_ID" = c."ID"
          )';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE NOT IN (-942, -904) THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'CREATE INDEX "idx_product_categories_store" ON "tb_product_categories" ("store_id")';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 AND SQLCODE != -942 AND SQLCODE != -904 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'CREATE INDEX "IDX_PRODUCT_CATEGORIES_STORE" ON "TB_PRODUCT_CATEGORIES" ("STORE_ID")';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 AND SQLCODE != -942 AND SQLCODE != -904 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE '
        CREATE UNIQUE INDEX "uk_product_categories_store_name_active"
        ON "tb_product_categories" (
            "store_id",
            LOWER("name"),
            "active"
        )';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 AND SQLCODE != -942 AND SQLCODE != -904 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE '
        CREATE UNIQUE INDEX "UK_PRODUCT_CATEGORIES_STORE_NAME_ACTIVE"
        ON "TB_PRODUCT_CATEGORIES" (
            "STORE_ID",
            LOWER("NAME"),
            "ACTIVE"
        )';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 AND SQLCODE != -942 AND SQLCODE != -904 THEN
            RAISE;
        END IF;
END;
/
