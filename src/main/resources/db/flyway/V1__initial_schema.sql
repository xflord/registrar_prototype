-- Form Specifications (no dependencies)
CREATE TABLE "form_specification" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "vo_id" VARCHAR(255),
    "group_id" VARCHAR(255) NOT NULL,
    "auto_approve" BOOLEAN DEFAULT FALSE,
    "auto_approve_extension" BOOLEAN DEFAULT FALSE
);

-- Destination (depends on form_specification)
CREATE TABLE "destination" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "urn" VARCHAR(255),
    "form_specification_id" INT,
    "global" BOOLEAN DEFAULT FALSE,
    FOREIGN KEY ("form_specification_id") REFERENCES "form_specification"("id") ON DELETE CASCADE
);

-- Item Definitions (depends on form_specification and destination)
CREATE TABLE "item_definition" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "form_specification_id" INT,
    "display_name" VARCHAR(255) NOT NULL,
    "type" VARCHAR(50) NOT NULL,
    "updatable" BOOLEAN,
    "required" BOOLEAN,
    "validator" VARCHAR(500),
    "destination_id" INT,
    "hidden" VARCHAR(50),
    "disabled" VARCHAR(50),
    "default_value" VARCHAR(500),
    "global" BOOLEAN DEFAULT FALSE,
    FOREIGN KEY ("form_specification_id") REFERENCES "form_specification"("id") ON DELETE CASCADE,
    FOREIGN KEY ("destination_id") REFERENCES "destination"("id") ON DELETE RESTRICT
);

-- Form Items (depends on form_specification and item_definition)
CREATE TABLE "form_item" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "form_id" INT NOT NULL,
    "short_name" VARCHAR(255),
    "parent_id" INT,
    "ord_num" INT NOT NULL,
    "hidden_dependency_item_id" INT,
    "disabled_dependency_item_id" INT,
    "item_definition_id" INT NOT NULL,
    FOREIGN KEY ("form_id") REFERENCES "form_specification"("id") ON DELETE CASCADE,
    FOREIGN KEY ("parent_id") REFERENCES "form_item"("id") ON DELETE SET NULL,
    FOREIGN KEY ("hidden_dependency_item_id") REFERENCES "form_item"("id") ON DELETE SET NULL,
    FOREIGN KEY ("disabled_dependency_item_id") REFERENCES "form_item"("id") ON DELETE SET NULL,
    FOREIGN KEY ("item_definition_id") REFERENCES "item_definition"("id") ON DELETE RESTRICT
);

-- Item Definition Form Types (depends on item_definition)
CREATE TABLE "item_definition_form_types" (
    "item_definition_id" INT NOT NULL,
    "form_type" VARCHAR(50) NOT NULL,
    PRIMARY KEY ("item_definition_id", "form_type"),
    FOREIGN KEY ("item_definition_id") REFERENCES "item_definition"("id") ON DELETE CASCADE
);

-- Item Texts (depends on item_definition)
CREATE TABLE "item_texts" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "item_definition_id" INT NOT NULL,
    "locale" VARCHAR(10) NOT NULL,
    "label" TEXT,
    "help" TEXT,
    "error" TEXT,
    FOREIGN KEY ("item_definition_id") REFERENCES "item_definition"("id") ON DELETE CASCADE,
    CONSTRAINT unique_item_locale UNIQUE ("item_definition_id", "locale")
);

-- Prefill Strategy Entries (depends on form_specification)
CREATE TABLE "prefill_strategy_entry" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "form_specification_id" INT,
    "type" VARCHAR(50) NOT NULL,
    "source_attribute" VARCHAR(255),
    "global" BOOLEAN DEFAULT FALSE,
    FOREIGN KEY ("form_specification_id") REFERENCES "form_specification"("id") ON DELETE CASCADE
);

-- Prefill Strategy Options (depends on prefill_strategy_entry)
CREATE TABLE "prefill_strategy_options" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "prefill_strategy_entry_id" INT NOT NULL,
    "option_key" VARCHAR(255) NOT NULL,
    "option_value" TEXT,
    FOREIGN KEY ("prefill_strategy_entry_id") REFERENCES "prefill_strategy_entry"("id") ON DELETE CASCADE,
    CONSTRAINT unique_strategy_option UNIQUE ("prefill_strategy_entry_id", "option_key")
);

-- Item Definition Prefill Strategies (depends on item_definition and prefill_strategy_entry)
CREATE TABLE "item_definition_prefill_strategies" (
    "item_definition_id" INT NOT NULL,
    "prefill_strategy_entry_id" INT NOT NULL,
    "position" INT NOT NULL,
    PRIMARY KEY ("item_definition_id", "prefill_strategy_entry_id"),
    FOREIGN KEY ("item_definition_id") REFERENCES "item_definition"("id") ON DELETE CASCADE,
    FOREIGN KEY ("prefill_strategy_entry_id") REFERENCES "prefill_strategy_entry"("id") ON DELETE RESTRICT,
    CONSTRAINT unique_item_strategy_position UNIQUE ("item_definition_id", "position")
);

-- Form Transitions (depends on form_specification)
CREATE TABLE "form_transition" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "source_form_specification_id" INT NOT NULL,
    "target_form_specification_id" INT NOT NULL,
    "position" INT NOT NULL,
    "target_form_state" VARCHAR(50) NOT NULL,
    "transition_type" VARCHAR(50) NOT NULL,
    FOREIGN KEY ("source_form_specification_id") REFERENCES "form_specification"("id") ON DELETE CASCADE,
    FOREIGN KEY ("target_form_specification_id") REFERENCES "form_specification"("id") ON DELETE CASCADE
);

-- Form Transition Source States (depends on form_transition)
CREATE TABLE "form_transition_source_states" (
    "form_transition_id" INT NOT NULL,
    "source_state" VARCHAR(50) NOT NULL,
    PRIMARY KEY ("form_transition_id", "source_state"),
    FOREIGN KEY ("form_transition_id") REFERENCES "form_transition"("id") ON DELETE CASCADE
);

-- Script Modules (no dependencies)
CREATE TABLE "script_module" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "name" VARCHAR(255) NOT NULL UNIQUE,
    "script" TEXT NOT NULL
);

-- Assigned Form Modules (depends on form_specification)
-- This table can reference either hardcoded modules (via module_name) or script modules (via script_module_id)
CREATE TABLE "assigned_form_module" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "form_id" INT NOT NULL,
    "position" INT NOT NULL,
    "module_name" VARCHAR(255),
    "script_module_id" INT,
    FOREIGN KEY ("form_id") REFERENCES "form_specification"("id") ON DELETE CASCADE,
    FOREIGN KEY ("script_module_id") REFERENCES "script_module"("id") ON DELETE CASCADE,
    CONSTRAINT unique_form_module_position UNIQUE ("form_id", "position"),
    CONSTRAINT chk_module_type CHECK (
        ("module_name" IS NOT NULL AND "script_module_id" IS NULL) OR
        ("module_name" IS NULL AND "script_module_id" IS NOT NULL)
    )
);

-- Form Module Options (depends on assigned_form_module)
CREATE TABLE "form_module_options" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "assigned_form_module_id" INT NOT NULL,
    "option_key" VARCHAR(255) NOT NULL,
    "option_value" TEXT,
    FOREIGN KEY ("assigned_form_module_id") REFERENCES "assigned_form_module"("id") ON DELETE CASCADE,
    CONSTRAINT unique_form_module_option UNIQUE ("assigned_form_module_id", "option_key")
);

-- Submissions (no dependencies)
CREATE TABLE "submission" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "timestamp" TIMESTAMP NOT NULL,
    "submitter_id" VARCHAR(255),
    "submitter_name" VARCHAR(255),
    "identity_identifier" VARCHAR(255),
    "identity_issuer" VARCHAR(255),
    "identity_attributes" TEXT
);

-- Applications (depends on form_specification and submission)
CREATE TABLE "application" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "idm_user_id" VARCHAR(255),
    "form_specification_id" INT NOT NULL,
    "type" VARCHAR(50) NOT NULL,
    "state" VARCHAR(50) NOT NULL,
    "redirect_url" VARCHAR(500),
    "submission_id" INT,
    FOREIGN KEY ("form_specification_id") REFERENCES "form_specification"("id") ON DELETE CASCADE,
    FOREIGN KEY ("submission_id") REFERENCES "submission"("id") ON DELETE SET NULL
);

-- Form Item Data (depends on application and form_item)
CREATE TABLE "form_item_data" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "application_id" INT NOT NULL,
    "form_item_id" INT NOT NULL,
    "value" TEXT,
    "prefilled_value" TEXT,
    "identity_attribute_value" TEXT,
    "idm_attribute_value" TEXT,
    "value_assured" BOOLEAN DEFAULT FALSE,
    FOREIGN KEY ("application_id") REFERENCES "application"("id") ON DELETE CASCADE,
    FOREIGN KEY ("form_item_id") REFERENCES "form_item"("id") ON DELETE CASCADE
);

CREATE TABLE "decision" (
    "id" INT PRIMARY KEY AUTO_INCREMENT,
    "application_id" INT NOT NULL,
    "approver_id" VARCHAR(255),
    "approver_name" TEXT,
    "message" TEXT,
    "timestamp" TIMESTAMP,
    "decision_type" VARCHAR(255),
    FOREIGN KEY ("application_id") REFERENCES "application"("id") ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_script_module_name ON "script_module"("name");
CREATE INDEX idx_assigned_form_module_form ON "assigned_form_module"("form_id");
CREATE INDEX idx_assigned_form_module_position ON "assigned_form_module"("form_id", "position");
CREATE INDEX idx_assigned_form_module_script ON "assigned_form_module"("script_module_id");
CREATE INDEX idx_submission_identity ON "submission"("identity_identifier", "identity_issuer");
CREATE INDEX idx_application_form ON "application"("form_specification_id");
CREATE INDEX idx_application_submission ON "application"("submission_id");
CREATE INDEX idx_application_state ON "application"("state");
CREATE INDEX idx_form_item_data_application ON "form_item_data"("application_id");
CREATE INDEX idx_form_item_data_form_item ON "form_item_data"("form_item_id");
