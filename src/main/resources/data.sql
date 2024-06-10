-- Drop existing tables if they exist
DROP TABLE IF EXISTS conversation_node_responses;
DROP TABLE IF EXISTS conversation_node;
DROP TABLE IF EXISTS chat_transaction;

-- Create conversation_node table if it doesn't exist
CREATE TABLE IF NOT EXISTS conversation_node (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 message VARCHAR(255),
                                                 message_name VARCHAR(255),
                                                 deletable BOOLEAN DEFAULT TRUE,
                                                 node_type VARCHAR(50) DEFAULT 'NORMAL_NODE'
);

-- Create conversation_node_responses table if it doesn't exist
CREATE TABLE IF NOT EXISTS conversation_node_responses (
                                                           conversation_node_id BIGINT,
                                                           response_key VARCHAR(255),
                                                           next_node_id BIGINT,
                                                           PRIMARY KEY (conversation_node_id, response_key),
                                                           FOREIGN KEY (conversation_node_id) REFERENCES conversation_node(id)
);

-- Create chat_transaction table if it doesn't exist
CREATE TABLE IF NOT EXISTS chat_transaction (
                                                id BIGSERIAL PRIMARY KEY,
                                                session_id BIGINT,
                                                message VARCHAR(255),
                                                sender VARCHAR(50),
                                                timestamp TIMESTAMP
);

-- Reset the sequence for conversation_node
ALTER SEQUENCE conversation_node_id_seq RESTART WITH 1;

-- Insert initial conversation nodes
INSERT INTO conversation_node (message, message_name, deletable, node_type) VALUES
                                                                                ('Hi! This is LISA. I have a great shift opportunity for you! Are you interested in hearing about it? Please respond "Yes" or "No"', '', FALSE, 'FIRST_NODE'),
                                                                                ('I''m sorry, I didn''t understand your response. Return to last request', 'Invalid Message', TRUE, 'INVALID_NODE'),
                                                                                ('Great the shift is at 1313 Mockingbird Ln at 2/15/2021 4:00pm-12:00am. We''ll see you there!', NULL, TRUE, 'NORMAL_NODE'),
                                                                                ('Ok, thanks. Can you let me know why not? Respond 1: Too far Respond 2: Not available Respond 3: Other', '', TRUE, 'NORMAL_NODE'),
                                                                                ('Thanks for letting me know. I''ll avoid offering shifts at this location in the future.', '', TRUE, 'NORMAL_NODE'),
                                                                                ('Thanks for letting me know. I''ll avoid offering shifts at this time in the future.', '', TRUE, 'NORMAL_NODE'),
                                                                                ('Ok. Thanks. I won''t offer shifts at this location or time in the future.', '', TRUE, 'NORMAL_NODE'),
                                                                                ('Do you want to restart the chat? Please respond "Yes" or "No"','', TRUE, 'END_NODE'),
                                                                                ('Thanks for chatting','', TRUE, 'END_NODE');


-- Insert initial responses
INSERT INTO conversation_node_responses (conversation_node_id, response_key, next_node_id) VALUES
                                                                                               (1, 'yes', 3),
                                                                                               (1, 'no', 4),
                                                                                               (4, '1', 5),
                                                                                               (4, '2', 6),
                                                                                               (4, '3', 7),
                                                                                               (8, 'yes', 1),
                                                                                               (8, 'no', 9);

