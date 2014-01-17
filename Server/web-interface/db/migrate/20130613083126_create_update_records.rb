class CreateUpdateRecords < ActiveRecord::Migration
  def change
    create_table :update_records do |t|
      t.integer :update_id
      t.integer :machine_id

      t.timestamps
    end
  end
end
