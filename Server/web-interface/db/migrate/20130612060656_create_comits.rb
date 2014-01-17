class CreateComits < ActiveRecord::Migration
  def change
    create_table :comits do |t|
      t.integer :repository_id
      t.string :comit_hash
      t.text :message
      t.boolean :is_update
      t.timestamp :time

      t.timestamps
    end
  end
end
