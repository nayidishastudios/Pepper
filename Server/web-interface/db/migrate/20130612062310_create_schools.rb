class CreateSchools < ActiveRecord::Migration
  def change
    create_table :schools do |t|
      t.integer :group_id
      t.string :name
      t.text :contact
      t.text :comments

      t.timestamps
    end
  end
end
