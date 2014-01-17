class Purchase < ActiveRecord::Base
  attr_accessible :repository_id, :typ, :typ_id
  belongs_to :repository

  validates :typ, numericality: { only_integer: true }, :presence => true, :inclusion => { :in => [ 0, 1, 2 ] }
  validates :typ_id, numericality: { only_integer: true }
  validate :typ_validity
  validate :purchase_uniqueness

  def typ_validity
    arr = [ "Group", "School", "Machine" ]
    if !Kernel.const_get(arr[typ]).exists?(typ_id)
      errors.add(:typ_id, "Please choose an appropriate " + arr[typ] + ".")
    end
  end

  def purchase_uniqueness
    purchase = Purchase.where(:typ => typ, :typ_id => typ_id, :repository_id => repository_id)
    if !purchase.empty?
      errors.add(:typ_id, "A similar purchase already exists")
    end
  end
end
