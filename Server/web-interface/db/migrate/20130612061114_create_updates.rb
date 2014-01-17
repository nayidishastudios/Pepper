class CreateUpdates < ActiveRecord::Migration
  def change
    create_table :updates do |t|
      t.integer :level
      t.integer :typ
      t.integer :typ_id
      t.integer :repository_id
      t.integer :comit_id

      t.timestamps
    end
  end
end
