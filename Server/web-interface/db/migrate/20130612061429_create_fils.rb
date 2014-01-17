class CreateFils < ActiveRecord::Migration
  def change
    create_table :fils do |t|
      t.integer :comit_id
      t.text :filename
      t.integer :action
      t.integer :size

      t.timestamps
    end
  end
end
