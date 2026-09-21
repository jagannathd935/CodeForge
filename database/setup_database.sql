-- =====================================================================
-- CodeForge: Master Database Setup Script
-- Description: Executes schema creation followed by seed data loading.
-- =====================================================================

source schema.sql;
source seed_data.sql;

SELECT 'Database codeforge_db setup completed successfully!' AS status;
