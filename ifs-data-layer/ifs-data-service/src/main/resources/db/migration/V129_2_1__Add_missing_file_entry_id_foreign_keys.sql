-- IFS-3948 - adding in missing foreign key constraints to the file_entry table
ALTER TABLE project ADD CONSTRAINT `grant_offer_letter_file_entry_id_fk` FOREIGN KEY (`grant_offer_letter_file_entry_id`) REFERENCES `file_entry` (`id`);
ALTER TABLE project ADD CONSTRAINT `additional_contract_file_entry_id_fk` FOREIGN KEY (`additional_contract_file_entry_id`) REFERENCES `file_entry` (`id`);
ALTER TABLE project ADD CONSTRAINT `signed_grant_offer_file_entry_id_fk` FOREIGN KEY (`signed_grant_offer_file_entry_id`) REFERENCES `file_entry` (`id`);
ALTER TABLE application_finance ADD CONSTRAINT `finance_file_entry_id_fk` FOREIGN KEY (`finance_file_entry_id`) REFERENCES `file_entry` (`id`);