-- Form Specifications
CREATE TABLE form_specification (
    id INT PRIMARY KEY AUTO_INCREMENT,
    vo_id VARCHAR(255),
    group_id VARCHAR(255) NOT NULL,
    auto_approve BOOLEAN DEFAULT FALSE,
    auto_approve_extension BOOLEAN DEFAULT FALSE
);

-- Form Items
CREATE TABLE form_item (
    id INT PRIMARY KEY AUTO_INCREMENT,
    form_id INT NOT NULL,
    short_name VARCHAR(255),
    parent_id INT,
    ord_num INT NOT NULL,
    hidden_dependency_item_id INT,
    disabled_dependency_item_id INT,
    item_definition_id INT NOT NULL,
    FOREIGN KEY (form_id) REFERENCES form_specification(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES form_item(id) ON DELETE SET NULL,
    FOREIGN KEY (hidden_dependency_item_id) REFERENCES form_item(id) ON DELETE SET NULL,
    FOREIGN KEY (disabled_dependency_item_id) REFERENCES form_item(id) ON DELETE SET NULL,
    FOREIGN KEY (item_definition_id) REFERENCES item_definition(id) ON DELETE RESTRICT
);

CREATE TABLE destination (
    id INT PRIMARY KEY AUTO_INCREMENT,
    urn VARCHAR(255),
    form_specification_id INT,
    global BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (form_specification_id) REFERENCES form_specification(id) ON DELETE CASCADE
)

-- Item Definitions
CREATE TABLE item_definition (
    id INT PRIMARY KEY AUTO_INCREMENT,
    form_specification_id INT,
    display_name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    updatable BOOLEAN,
    required BOOLEAN,
    validator VARCHAR(500),
    destination_id INT,
    hidden VARCHAR(50),
    disabled VARCHAR(50),
    default_value VARCHAR(500),
    global BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (form_specification_id) REFERENCES form_specification(id) ON DELETE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES destination(id) ON DELETE RESTRICT
);

-- Item Definition Form Types (many-to-many)
CREATE TABLE item_definition_form_types (
    item_definition_id INT NOT NULL,
    form_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (item_definition_id, form_type),
    FOREIGN KEY (item_definition_id) REFERENCES item_definition(id) ON DELETE CASCADE
);

-- Item Texts (stored as JSON or separate table - using separate for clarity)
CREATE TABLE item_texts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    item_definition_id INT NOT NULL,
    locale VARCHAR(10) NOT NULL,
    label TEXT,
    help_text TEXT,
    error_text TEXT,
    FOREIGN KEY (item_definition_id) REFERENCES item_definition(id) ON DELETE CASCADE,
    UNIQUE KEY unique_item_locale (item_definition_id, locale)
);

-- Prefill Strategy Entries
CREATE TABLE prefill_strategy_entry (
    id INT PRIMARY KEY AUTO_INCREMENT,
    form_specification_id INT,
    type VARCHAR(50) NOT NULL,
    source_attribute VARCHAR(255),
    global BOOLEAN DEFAULT FALSE
);

-- Prefill Strategy Options (stored as key-value pairs)
CREATE TABLE prefill_strategy_options (
    id INT PRIMARY KEY AUTO_INCREMENT,
    prefill_strategy_entry_id INT NOT NULL,
    option_key VARCHAR(255) NOT NULL,
    option_value TEXT,
    FOREIGN KEY (prefill_strategy_entry_id) REFERENCES prefill_strategy_entry(id) ON DELETE CASCADE,
    UNIQUE KEY unique_strategy_option (prefill_strategy_entry_id, option_key)
);

-- Item Definition Prefill Strategies (many-to-many)
CREATE TABLE item_definition_prefill_strategies (
    item_definition_id INT NOT NULL,
    prefill_strategy_entry_id INT NOT NULL,
    position INT NOT NULL,
    PRIMARY KEY (item_definition_id, prefill_strategy_entry_id),
    FOREIGN KEY (item_definition_id) REFERENCES item_definition(id) ON DELETE CASCADE,
    FOREIGN KEY (prefill_strategy_entry_id) REFERENCES prefill_strategy_entry(id) ON DELETE RESTRICT
);

-- Form Transitions
CREATE TABLE form_transition (
    id INT PRIMARY KEY AUTO_INCREMENT,
    source_form_specification_id INT NOT NULL,
    target_form_specification_id INT NOT NULL,
    position INT NOT NULL,
    target_form_state VARCHAR(50) NOT NULL,
    transition_type VARCHAR(50) NOT NULL,
    FOREIGN KEY (source_form_specification_id) REFERENCES form_specification(id) ON DELETE CASCADE,
    FOREIGN KEY (target_form_specification_id) REFERENCES form_specification(id) ON DELETE CASCADE
);

-- Form Transition Source States (many-to-many)
CREATE TABLE form_transition_source_states (
    form_transition_id INT NOT NULL,
    source_state VARCHAR(50) NOT NULL,
    position INT NOT NULL,
    PRIMARY KEY (form_transition_id, source_state),
    FOREIGN KEY (form_transition_id) REFERENCES form_transition(id) ON DELETE CASCADE,
    UNIQUE KEY unique_transition_state_position (form_transition_id, position)
);

-- Assigned Form Modules
-- TODO link this with the ScriptModule table most likely
CREATE TABLE assigned_form_module (
    id INT PRIMARY KEY AUTO_INCREMENT,
    form_id INT NOT NULL,
    position INT NOT NULL,
    module_name VARCHAR(255) NOT NULL,
    FOREIGN KEY (form_id) REFERENCES form_specification(id) ON DELETE CASCADE,
    UNIQUE KEY unique_form_module (form_id, module_name)
);

-- Form Module Options (key-value pairs)
CREATE TABLE form_module_options (
    id INT PRIMARY KEY AUTO_INCREMENT,
    assigned_form_module_id INT NOT NULL,
    option_key VARCHAR(255) NOT NULL,
    option_value TEXT,
    FOREIGN KEY (assigned_form_module_id) REFERENCES assigned_form_module(id) ON DELETE CASCADE,
    UNIQUE KEY unique_module_option (assigned_form_module_id, option_key)
);

-- Indexes for performance
CREATE INDEX idx_form_item_form_id ON form_item(form_id);
CREATE INDEX idx_form_item_item_definition_id ON form_item(item_definition_id);
CREATE INDEX idx_item_definition_form_spec_id ON item_definition(form_specification_id);
CREATE INDEX idx_item_definition_global ON item_definition(global);
CREATE INDEX idx_prefill_strategy_global ON prefill_strategy_entry(global);
CREATE INDEX idx_form_transition_source ON form_transition(source_form_specification_id);
CREATE INDEX idx_form_transition_target ON form_transition(target_form_specification_id);